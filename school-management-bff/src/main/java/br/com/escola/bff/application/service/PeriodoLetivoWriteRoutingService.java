package br.com.escola.bff.application.service;

import java.util.UUID;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import br.com.escola.bff.application.port.out.AcademicCatalogPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.MonolithPeriodoLetivoWritePort;
import br.com.escola.bff.application.usecase.CreatePeriodoLetivoUseCase;
import reactor.core.publisher.Mono;

public class PeriodoLetivoWriteRoutingService implements CreatePeriodoLetivoUseCase {

    private final MonolithPeriodoLetivoWritePort monolithWritePort;
    private final AcademicCatalogPeriodoLetivoWritePort catalogWritePort;
    private final AuthContextPort authContextPort;
    private final CatalogWriteCutoverPolicyPort cutoverPolicyPort;
    private final CatalogWriteObservabilityPort observabilityPort;

    public PeriodoLetivoWriteRoutingService(
            MonolithPeriodoLetivoWritePort monolithWritePort,
            AcademicCatalogPeriodoLetivoWritePort catalogWritePort,
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
    public Mono<PeriodoLetivoCreatedResult> executar(CatalogWriteQuery query, PeriodoLetivoCreateCommand command) {
        CatalogWriteCutoverDecision decision = cutoverPolicyPort.decision(CatalogWriteRoute.PERIODOS_LETIVOS);
        if (!decision.useCatalog()) {
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectMonolith(decision));
        }

        return authContextPort.resolve(new CatalogReadQuery(query.authorization(), query.correlationId()))
                .map(context -> validateScope(context, command))
                .flatMap(context -> catalogWritePort.criar(query, context, command)
                        .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision)))
                .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error));
    }

    private AuthSessionContext validateScope(AuthSessionContext context, PeriodoLetivoCreateCommand command) {
        UUID requestedEscolaId = command.escolaId();
        if (requestedEscolaId != null && !requestedEscolaId.equals(context.escolaId())) {
            throw new IllegalArgumentException("escolaId informado diverge do contexto autenticado");
        }
        return context;
    }
}
