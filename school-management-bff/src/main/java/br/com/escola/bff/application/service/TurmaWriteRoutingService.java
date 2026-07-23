package br.com.escola.bff.application.service;

import java.util.UUID;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import br.com.escola.bff.application.dto.TurnoResolved;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogoTurmaWritePort;
import br.com.escola.bff.application.port.out.CatalogoTurnoResolverPort;
import br.com.escola.bff.application.usecase.CreateTurmaUseCase;
import reactor.core.publisher.Mono;

public class TurmaWriteRoutingService implements CreateTurmaUseCase {

    private final CatalogoTurmaWritePort catalogWritePort;
    private final CatalogoTurnoResolverPort turnoResolverPort;
    private final AuthContextPort authContextPort;
    private final CatalogWriteObservabilityPort observabilityPort;

    public TurmaWriteRoutingService(
            CatalogoTurmaWritePort catalogWritePort,
            CatalogoTurnoResolverPort turnoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        this.catalogWritePort = catalogWritePort;
        this.turnoResolverPort = turnoResolverPort;
        this.authContextPort = authContextPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<TurmaCreatedResult> executar(CatalogWriteQuery query, TurmaCreateCommand command) {
        CatalogWriteCutoverDecision decision = new CatalogWriteCutoverDecision(
                CatalogWriteRoute.TURMAS,
                true,
                "catalog_official");
        return authContextPort.resolve(new CatalogReadQuery(query.authorization(), query.correlationId()))
                .map(context -> validateScope(context, command))
                .flatMap(context -> resolveTurno(query, context, command)
                        .flatMap(turno -> catalogWritePort.criar(query, context, turno, command)
                                .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision))))
                .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error));
    }

    private Mono<TurnoResolved> resolveTurno(
            CatalogWriteQuery query,
            AuthSessionContext context,
            TurmaCreateCommand command) {
        if (command.capacidade() == null || command.capacidade() <= 0) {
            return Mono.error(new IllegalArgumentException("capacidade informada deve ser maior que zero"));
        }
        if (command.periodoLetivoId() == null) {
            return Mono.error(new IllegalArgumentException("periodoLetivoId informado e obrigatorio"));
        }
        if (command.serieId() == null) {
            return Mono.error(new IllegalArgumentException("serieId informado e obrigatorio"));
        }
        if (command.turno() == null || command.turno().isBlank()) {
            return Mono.error(new IllegalArgumentException("turno informado e obrigatorio"));
        }
        if (command.status() != null
                && !command.status().isBlank()
                && !"ATIVA".equalsIgnoreCase(command.status().trim())) {
            return Mono.error(new IllegalArgumentException("status informado deve ser ATIVA quando preenchido"));
        }
        return turnoResolverPort.resolve(query, context, command.turno())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "turno informado nao foi encontrado no catalogo oficial")));
    }

    private AuthSessionContext validateScope(AuthSessionContext context, TurmaCreateCommand command) {
        UUID requestedEscolaId = command.escolaId();
        if (requestedEscolaId != null && !requestedEscolaId.equals(context.escolaId())) {
            throw new IllegalArgumentException("escolaId informado diverge do contexto autenticado");
        }
        return context;
    }
}

