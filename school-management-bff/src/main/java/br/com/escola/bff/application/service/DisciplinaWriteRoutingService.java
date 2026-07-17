package br.com.escola.bff.application.service;

import java.util.Locale;
import java.util.UUID;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import br.com.escola.bff.application.port.out.CatalogoDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.LegacyDisciplinaWritePort;
import br.com.escola.bff.application.usecase.CreateDisciplinaUseCase;
import reactor.core.publisher.Mono;

public class DisciplinaWriteRoutingService implements CreateDisciplinaUseCase {

    private final LegacyDisciplinaWritePort monolithWritePort;
    private final CatalogoDisciplinaWritePort catalogWritePort;
    private final AuthContextPort authContextPort;
    private final CatalogWriteCutoverPolicyPort cutoverPolicyPort;
    private final CatalogWriteObservabilityPort observabilityPort;

    public DisciplinaWriteRoutingService(
            LegacyDisciplinaWritePort monolithWritePort,
            CatalogoDisciplinaWritePort catalogWritePort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        this.monolithWritePort = monolithWritePort;
        this.catalogWritePort = catalogWritePort;
        this.authContextPort = authContextPort;
        this.cutoverPolicyPort = cutoverPolicyPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<DisciplinaCreatedResult> executar(CatalogWriteQuery query, DisciplinaCreateCommand command) {
        CatalogWriteCutoverDecision decision = cutoverPolicyPort.decision(CatalogWriteRoute.DISCIPLINAS);
        if (!decision.useCatalog()) {
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectLegacy(decision));
        }

        if (!isCatalogCompatible(command.status())) {
            CatalogWriteCutoverDecision unsupportedDecision =
                    new CatalogWriteCutoverDecision(CatalogWriteRoute.DISCIPLINAS, false, "unsupported_status");
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectLegacy(unsupportedDecision));
        }

        return authContextPort.resolve(new CatalogReadQuery(query.authorization(), query.correlationId()))
                .map(context -> validateScope(context, command))
                .flatMap(context -> catalogWritePort.criar(query, context, command)
                        .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision)))
                .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error));
    }

    private boolean isCatalogCompatible(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return "ATIVA".equals(status.trim().toUpperCase(Locale.ROOT));
    }

    private AuthSessionContext validateScope(AuthSessionContext context, DisciplinaCreateCommand command) {
        UUID requestedEscolaId = command.escolaId();
        if (requestedEscolaId != null && !requestedEscolaId.equals(context.escolaId())) {
            throw new IllegalArgumentException("escolaId informado diverge do contexto autenticado");
        }
        return context;
    }
}

