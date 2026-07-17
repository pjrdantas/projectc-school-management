package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.NivelEnsinoResolved;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.CatalogoSerieWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.LegacySerieWritePort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SerieWriteRoutingServiceTest {

    @Test
    void deveUsarMonolitoQuandoCutoverNaoEstiverLiberado() {
        LegacySerieWritePort monolith = (query, command) -> Mono.just(resultado("ENSINO_FUNDAMENTAL", "monolith"));
        CatalogoSerieWritePort catalog = (query, context, nivel, command) -> Mono.just(resultado("ENSINO_FUNDAMENTAL", "catalog"));
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) ->
                Mono.just(new NivelEnsinoResolved(UUID.randomUUID(), "ENSINO_FUNDAMENTAL"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, false, "cutover_disabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("ENSINO_FUNDAMENTAL", null)))
                .assertNext(response -> assertThat(response.escolaNome()).isEqualTo("monolith"))
                .verifyComplete();
    }

    @Test
    void deveUsarMonolitoQuandoNivelEnsinoNaoVierInformado() {
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        LegacySerieWritePort monolith = (query, command) -> Mono.just(resultado(null, "monolith"));
        CatalogoSerieWritePort catalog = (query, context, nivel, command) -> Mono.just(resultado("ENSINO_FUNDAMENTAL", "catalog"));
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) -> {
            resolverCalled.set(true);
            return Mono.just(new NivelEnsinoResolved(UUID.randomUUID(), "ENSINO_FUNDAMENTAL"));
        };
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command(" ", null)))
                .assertNext(response -> assertThat(response.escolaNome()).isEqualTo("monolith"))
                .verifyComplete();

        assertThat(resolverCalled.get()).isFalse();
    }

    @Test
    void deveUsarMonolitoQuandoNivelEnsinoNaoForResolvidoNoCatalogoNovo() {
        AtomicBoolean catalogCalled = new AtomicBoolean(false);
        LegacySerieWritePort monolith = (query, command) -> Mono.just(resultado(command.nivelEnsino(), "monolith"));
        CatalogoSerieWritePort catalog = (query, context, nivel, command) -> {
            catalogCalled.set(true);
            return Mono.just(resultado("ENSINO_FUNDAMENTAL", "catalog"));
        };
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) -> Mono.empty();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("ENSINO_MEDIO", null)))
                .assertNext(response -> assertThat(response.escolaNome()).isEqualTo("monolith"))
                .verifyComplete();

        assertThat(catalogCalled.get()).isFalse();
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        AtomicBoolean monolithCalled = new AtomicBoolean(false);
        LegacySerieWritePort monolith = (query, command) -> {
            monolithCalled.set(true);
            return Mono.just(resultado(command.nivelEnsino(), "monolith"));
        };
        CatalogoSerieWritePort catalog = (query, context, nivel, command) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) ->
                Mono.just(new NivelEnsinoResolved(UUID.randomUUID(), "ENSINO_FUNDAMENTAL"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("ENSINO_FUNDAMENTAL", null)))
                .expectError(DownstreamUnavailableException.class)
                .verify();

        assertThat(monolithCalled.get()).isFalse();
    }

    @Test
    void deveRejeitarEscolaIdDiferenteDoContextoAutenticado() {
        UUID escolaContexto = UUID.randomUUID();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), escolaContexto, "Escola A"));
        LegacySerieWritePort monolith = (query, command) -> Mono.just(resultado(command.nivelEnsino(), "monolith"));
        CatalogoSerieWritePort catalog = (query, context, nivel, command) -> Mono.just(resultado("ENSINO_FUNDAMENTAL", "catalog"));
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) ->
                Mono.just(new NivelEnsinoResolved(UUID.randomUUID(), "ENSINO_FUNDAMENTAL"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                monolith, catalog, resolver, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("ENSINO_FUNDAMENTAL", UUID.randomUUID())))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("escolaId informado diverge"))
                .verify();
    }

    private static CatalogWriteQuery query() {
        return new CatalogWriteQuery("Bearer token", "corr-1", "idem-1");
    }

    private static SerieCreateCommand command(String nivelEnsino, UUID escolaId) {
        return new SerieCreateCommand("1 ano", 1, nivelEnsino, escolaId);
    }

    private static SerieCreatedResult resultado(String nivelEnsino, String escolaNome) {
        return new SerieCreatedResult(
                UUID.randomUUID(),
                "1 ano",
                1,
                nivelEnsino,
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

