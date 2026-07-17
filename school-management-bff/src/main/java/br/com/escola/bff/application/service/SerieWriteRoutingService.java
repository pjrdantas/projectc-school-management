package br.com.escola.bff.application.service;

import java.util.UUID;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.NivelEnsinoResolved;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import br.com.escola.bff.application.port.out.CatalogoNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.CatalogoSerieWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.usecase.CreateSerieUseCase;
import reactor.core.publisher.Mono;

public class SerieWriteRoutingService implements CreateSerieUseCase {

    private final CatalogoSerieWritePort catalogWritePort;
    private final CatalogoNivelEnsinoResolverPort nivelEnsinoResolverPort;
    private final AuthContextPort authContextPort;
    private final CatalogWriteObservabilityPort observabilityPort;

    public SerieWriteRoutingService(
            CatalogoSerieWritePort catalogWritePort,
            CatalogoNivelEnsinoResolverPort nivelEnsinoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        this.catalogWritePort = catalogWritePort;
        this.nivelEnsinoResolverPort = nivelEnsinoResolverPort;
        this.authContextPort = authContextPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<SerieCreatedResult> executar(CatalogWriteQuery query, SerieCreateCommand command) {
        CatalogWriteCutoverDecision decision = new CatalogWriteCutoverDecision(
                CatalogWriteRoute.SERIES,
                true,
                "catalog_official");
        return authContextPort.resolve(new CatalogReadQuery(query.authorization(), query.correlationId()))
                .map(context -> validateScope(context, command))
                .flatMap(context -> resolveNivelEnsino(query, context, command)
                        .flatMap(nivelEnsino -> catalogWritePort.criar(query, context, nivelEnsino, command)
                                .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision))))
                .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error));
    }

    private Mono<NivelEnsinoResolved> resolveNivelEnsino(
            CatalogWriteQuery query,
            AuthSessionContext context,
            SerieCreateCommand command) {
        if (command.ordem() == null || command.ordem() <= 0) {
            return Mono.error(new IllegalArgumentException("ordem informada deve ser maior que zero"));
        }
        if (command.nivelEnsino() == null || command.nivelEnsino().isBlank()) {
            return Mono.error(new IllegalArgumentException("nivelEnsino informado e obrigatorio"));
        }
        return nivelEnsinoResolverPort.resolve(query, context, command.nivelEnsino())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "nivelEnsino informado nao foi encontrado no catalogo oficial")));
    }

    private AuthSessionContext validateScope(AuthSessionContext context, SerieCreateCommand command) {
        UUID requestedEscolaId = command.escolaId();
        if (requestedEscolaId != null && !requestedEscolaId.equals(context.escolaId())) {
            throw new IllegalArgumentException("escolaId informado diverge do contexto autenticado");
        }
        return context;
    }
}

