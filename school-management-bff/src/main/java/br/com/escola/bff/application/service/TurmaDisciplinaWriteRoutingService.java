package br.com.escola.bff.application.service;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkCommand;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkedResult;
import br.com.escola.bff.application.port.out.CatalogoTurmaDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.usecase.LinkTurmaDisciplinaUseCase;
import reactor.core.publisher.Mono;

public class TurmaDisciplinaWriteRoutingService implements LinkTurmaDisciplinaUseCase {

    private final CatalogoTurmaDisciplinaWritePort catalogWritePort;
    private final AuthContextPort authContextPort;
    private final CatalogWriteObservabilityPort observabilityPort;

    public TurmaDisciplinaWriteRoutingService(
            CatalogoTurmaDisciplinaWritePort catalogWritePort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        this.catalogWritePort = catalogWritePort;
        this.authContextPort = authContextPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<TurmaDisciplinaLinkedResult> executar(CatalogWriteQuery query, TurmaDisciplinaLinkCommand command) {
        CatalogWriteCutoverDecision decision = new CatalogWriteCutoverDecision(
                CatalogWriteRoute.TURMA_DISCIPLINAS,
                true,
                "catalog_official");
        return authContextPort.resolve(new CatalogReadQuery(query.authorization(), query.correlationId()))
                .flatMap(context -> catalogWritePort.vincular(query, context, command)
                        .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision)))
                .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error));
    }
}

