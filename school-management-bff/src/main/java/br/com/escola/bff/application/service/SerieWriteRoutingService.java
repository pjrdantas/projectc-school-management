package br.com.escola.bff.application.service;

import java.util.UUID;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import br.com.escola.bff.application.port.out.CatalogoNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.CatalogoSerieWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.LegacySerieWritePort;
import br.com.escola.bff.application.usecase.CreateSerieUseCase;
import reactor.core.publisher.Mono;

public class SerieWriteRoutingService implements CreateSerieUseCase {

    private final LegacySerieWritePort monolithWritePort;
    private final CatalogoSerieWritePort catalogWritePort;
    private final CatalogoNivelEnsinoResolverPort nivelEnsinoResolverPort;
    private final AuthContextPort authContextPort;
    private final CatalogWriteCutoverPolicyPort cutoverPolicyPort;
    private final CatalogWriteObservabilityPort observabilityPort;

    public SerieWriteRoutingService(
            LegacySerieWritePort monolithWritePort,
            CatalogoSerieWritePort catalogWritePort,
            CatalogoNivelEnsinoResolverPort nivelEnsinoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        this.monolithWritePort = monolithWritePort;
        this.catalogWritePort = catalogWritePort;
        this.nivelEnsinoResolverPort = nivelEnsinoResolverPort;
        this.authContextPort = authContextPort;
        this.cutoverPolicyPort = cutoverPolicyPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<SerieCreatedResult> executar(CatalogWriteQuery query, SerieCreateCommand command) {
        CatalogWriteCutoverDecision decision = cutoverPolicyPort.decision(CatalogWriteRoute.SERIES);
        if (!decision.useCatalog()) {
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectLegacy(decision));
        }

        if (!isCatalogCompatible(command)) {
            CatalogWriteCutoverDecision unsupportedDecision =
                    new CatalogWriteCutoverDecision(CatalogWriteRoute.SERIES, false, "unsupported_nivel_ensino");
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectLegacy(unsupportedDecision));
        }

        return authContextPort.resolve(new CatalogReadQuery(query.authorization(), query.correlationId()))
                .map(context -> validateScope(context, command))
                .flatMap(context -> nivelEnsinoResolverPort.resolve(query, context, command.nivelEnsino())
                        .flatMap(nivelEnsino -> catalogWritePort.criar(query, context, nivelEnsino, command)
                                .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision)))
                        .switchIfEmpty(routeToLegacyForUnsupportedNivel(query, command)))
                .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error));
    }

    private Mono<SerieCreatedResult> routeToLegacyForUnsupportedNivel(
            CatalogWriteQuery query,
            SerieCreateCommand command) {
        return Mono.defer(() -> {
            CatalogWriteCutoverDecision unsupportedDecision =
                    new CatalogWriteCutoverDecision(CatalogWriteRoute.SERIES, false, "unsupported_nivel_ensino");
            return monolithWritePort.criar(query, command)
                    .doOnSuccess(response -> observabilityPort.recordDirectLegacy(unsupportedDecision));
        });
    }

    private boolean isCatalogCompatible(SerieCreateCommand command) {
        return command.ordem() != null
                && command.ordem() > 0
                && command.nivelEnsino() != null
                && !command.nivelEnsino().isBlank();
    }

    private AuthSessionContext validateScope(AuthSessionContext context, SerieCreateCommand command) {
        UUID requestedEscolaId = command.escolaId();
        if (requestedEscolaId != null && !requestedEscolaId.equals(context.escolaId())) {
            throw new IllegalArgumentException("escolaId informado diverge do contexto autenticado");
        }
        return context;
    }
}

