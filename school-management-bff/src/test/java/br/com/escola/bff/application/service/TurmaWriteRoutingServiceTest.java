package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import br.com.escola.bff.application.dto.TurnoResolved;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoTurnoResolverPort;
import br.com.escola.bff.application.port.out.CatalogoTurmaWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.LegacyTurmaWritePort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TurmaWriteRoutingServiceTest {

    @Test
    void deveUsarMonolitoQuandoCutoverNaoEstiverLiberado() {
        LegacyTurmaWritePort monolith = (query, command) -> Mono.just(resultado("MANHA", "ATIVA", "monolith"));
        CatalogoTurmaWritePort catalog = (query, context, turno, command) ->
                Mono.just(resultado("MANHA", "ATIVA", "catalog"));
        CatalogoTurnoResolverPort resolver = (query, context, turno) ->
                Mono.just(new TurnoResolved(UUID.randomUUID(), "MANHA"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, false, "cutover_disabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("MANHA", "ATIVA", null)))
                .assertNext(response -> assertThat(response.escolaNome()).isEqualTo("monolith"))
                .verifyComplete();
    }

    @Test
    void deveUsarMonolitoQuandoStatusNaoForCompativelComCatalogoNovo() {
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        LegacyTurmaWritePort monolith = (query, command) -> Mono.just(resultado(command.turno(), command.status(), "monolith"));
        CatalogoTurmaWritePort catalog = (query, context, turno, command) ->
                Mono.just(resultado("MANHA", "ATIVA", "catalog"));
        CatalogoTurnoResolverPort resolver = (query, context, turno) -> {
            resolverCalled.set(true);
            return Mono.just(new TurnoResolved(UUID.randomUUID(), "MANHA"));
        };
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("MANHA", "INATIVA", null)))
                .assertNext(response -> assertThat(response.status()).isEqualTo("INATIVA"))
                .verifyComplete();

        assertThat(resolverCalled.get()).isFalse();
    }

    @Test
    void deveUsarMonolitoQuandoTurnoNaoForResolvidoNoCatalogoNovo() {
        AtomicBoolean catalogCalled = new AtomicBoolean(false);
        LegacyTurmaWritePort monolith = (query, command) -> Mono.just(resultado(command.turno(), "ATIVA", "monolith"));
        CatalogoTurmaWritePort catalog = (query, context, turno, command) -> {
            catalogCalled.set(true);
            return Mono.just(resultado("MANHA", "ATIVA", "catalog"));
        };
        CatalogoTurnoResolverPort resolver = (query, context, turno) -> Mono.empty();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("NOITE", "ATIVA", null)))
                .assertNext(response -> assertThat(response.escolaNome()).isEqualTo("monolith"))
                .verifyComplete();

        assertThat(catalogCalled.get()).isFalse();
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        AtomicBoolean monolithCalled = new AtomicBoolean(false);
        LegacyTurmaWritePort monolith = (query, command) -> {
            monolithCalled.set(true);
            return Mono.just(resultado(command.turno(), command.status(), "monolith"));
        };
        CatalogoTurmaWritePort catalog = (query, context, turno, command) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        CatalogoTurnoResolverPort resolver = (query, context, turno) ->
                Mono.just(new TurnoResolved(UUID.randomUUID(), "MANHA"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("MANHA", "ATIVA", null)))
                .expectError(DownstreamUnavailableException.class)
                .verify();

        assertThat(monolithCalled.get()).isFalse();
    }

    @Test
    void deveRejeitarEscolaIdDiferenteDoContextoAutenticado() {
        UUID escolaContexto = UUID.randomUUID();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), escolaContexto, "Escola A"));
        LegacyTurmaWritePort monolith = (query, command) -> Mono.just(resultado(command.turno(), command.status(), "monolith"));
        CatalogoTurmaWritePort catalog = (query, context, turno, command) ->
                Mono.just(resultado("MANHA", "ATIVA", "catalog"));
        CatalogoTurnoResolverPort resolver = (query, context, turno) ->
                Mono.just(new TurnoResolved(UUID.randomUUID(), "MANHA"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("MANHA", "ATIVA", UUID.randomUUID())))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("escolaId informado diverge"))
                .verify();
    }

    private static CatalogWriteQuery query() {
        return new CatalogWriteQuery("Bearer token", "corr-1", "idem-1");
    }

    private static TurmaCreateCommand command(String turno, String status, UUID escolaId) {
        return new TurmaCreateCommand(
                "A",
                "Turma A",
                30,
                UUID.randomUUID(),
                UUID.randomUUID(),
                turno,
                status,
                escolaId);
    }

    private static TurmaCreatedResult resultado(String turno, String status, String escolaNome) {
        return new TurmaCreatedResult(
                UUID.randomUUID(),
                "A",
                "Turma A",
                30,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "1 ano",
                turno,
                status,
                UUID.randomUUID(),
                escolaNome,
                LocalDateTime.now());
    }

    private static final class NoOpObservability implements CatalogWriteObservabilityPort {
        @Override public void recordDirectLegacy(CatalogWriteCutoverDecision decision) {}
        @Override public void recordCatalogSuccess(CatalogWriteCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogWriteCutoverDecision decision, Throwable error) {}
    }
}

