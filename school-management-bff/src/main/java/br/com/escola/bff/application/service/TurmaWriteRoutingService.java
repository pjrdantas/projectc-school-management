package br.com.escola.bff.application.service;

import java.util.Locale;
import java.util.UUID;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import br.com.escola.bff.application.port.out.AcademicCatalogTurnoResolverPort;
import br.com.escola.bff.application.port.out.AcademicCatalogTurmaWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.MonolithTurmaWritePort;
import br.com.escola.bff.application.usecase.CreateTurmaUseCase;
import reactor.core.publisher.Mono;

public class TurmaWriteRoutingService implements CreateTurmaUseCase {

    private final MonolithTurmaWritePort monolithWritePort;
    private final AcademicCatalogTurmaWritePort catalogWritePort;
    private final AcademicCatalogTurnoResolverPort turnoResolverPort;
    private final AuthContextPort authContextPort;
    private final CatalogWriteCutoverPolicyPort cutoverPolicyPort;
    private final CatalogWriteObservabilityPort observabilityPort;

    public TurmaWriteRoutingService(
            MonolithTurmaWritePort monolithWritePort,
            AcademicCatalogTurmaWritePort catalogWritePort,
            AcademicCatalogTurnoResolverPort turnoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        this.monolithWritePort = monolithWritePort;
        this.catalogWritePort = catalogWritePort;
        this.turnoResolverPort = turnoResolverPort;
        this.authContextPort = authContextPort;
        this.cutoverPolicyPort = cutoverPolicyPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<TurmaCreatedResult> executar(CatalogWriteQuery query, TurmaCreateCommand command) {
        CatalogWriteCutoverDecision decision = cutoverPolicyPort.decision(CatalogWriteRoute.TURMAS);
        if (!decision.useCatalog()) {
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectMonolith(decision));
        }

        if (!isCatalogCompatible(command)) {
            CatalogWriteCutoverDecision unsupportedDecision =
                    new CatalogWriteCutoverDecision(CatalogWriteRoute.TURMAS, false, "unsupported_turno_or_status");
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectMonolith(unsupportedDecision));
        }

        return authContextPort.resolve(new CatalogReadQuery(query.authorization(), query.correlationId()))
                .map(context -> validateScope(context, command))
                .flatMap(context -> turnoResolverPort.resolve(query, context, command.turno())
                        .flatMap(turno -> catalogWritePort.criar(query, context, turno, command)
                                .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision)))
                        .switchIfEmpty(routeToMonolithForUnsupportedTurno(query, command)))
                .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error));
    }

    private Mono<TurmaCreatedResult> routeToMonolithForUnsupportedTurno(
            CatalogWriteQuery query,
            TurmaCreateCommand command) {
        return Mono.defer(() -> {
            CatalogWriteCutoverDecision unsupportedDecision =
                    new CatalogWriteCutoverDecision(CatalogWriteRoute.TURMAS, false, "unsupported_turno_or_status");
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectMonolith(unsupportedDecision));
        });
    }

    private boolean isCatalogCompatible(TurmaCreateCommand command) {
        return command.capacidade() != null
                && command.capacidade() > 0
                && command.periodoLetivoId() != null
                && command.serieId() != null
                && command.turno() != null
                && !command.turno().isBlank()
                && isCatalogCompatibleStatus(command.status());
    }

    private boolean isCatalogCompatibleStatus(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return "ATIVA".equals(status.trim().toUpperCase(Locale.ROOT));
    }

    private AuthSessionContext validateScope(AuthSessionContext context, TurmaCreateCommand command) {
        UUID requestedEscolaId = command.escolaId();
        if (requestedEscolaId != null && !requestedEscolaId.equals(context.escolaId())) {
            throw new IllegalArgumentException("escolaId informado diverge do contexto autenticado");
        }
        return context;
    }
}
