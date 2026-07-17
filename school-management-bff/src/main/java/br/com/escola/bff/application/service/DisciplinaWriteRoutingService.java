package br.com.escola.bff.application.service;

import java.util.UUID;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogoDisciplinaWritePort;
import br.com.escola.bff.application.usecase.CreateDisciplinaUseCase;
import reactor.core.publisher.Mono;

public class DisciplinaWriteRoutingService implements CreateDisciplinaUseCase {

    private final CatalogoDisciplinaWritePort catalogWritePort;
    private final AuthContextPort authContextPort;
    private final CatalogWriteObservabilityPort observabilityPort;

    public DisciplinaWriteRoutingService(
            CatalogoDisciplinaWritePort catalogWritePort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        this.catalogWritePort = catalogWritePort;
        this.authContextPort = authContextPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<DisciplinaCreatedResult> executar(CatalogWriteQuery query, DisciplinaCreateCommand command) {
        CatalogWriteCutoverDecision decision = new CatalogWriteCutoverDecision(
                CatalogWriteRoute.DISCIPLINAS,
                true,
                "catalog_official");
        return authContextPort.resolve(new CatalogReadQuery(query.authorization(), query.correlationId()))
                .map(context -> validateScope(context, command))
                .flatMap(context -> catalogWritePort.criar(query, context, command)
                        .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision)))
                .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error));
    }

    private AuthSessionContext validateScope(AuthSessionContext context, DisciplinaCreateCommand command) {
        UUID requestedEscolaId = command.escolaId();
        if (requestedEscolaId != null && !requestedEscolaId.equals(context.escolaId())) {
            throw new IllegalArgumentException("escolaId informado diverge do contexto autenticado");
        }
        return context;
    }
}

