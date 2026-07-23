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
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogoNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.CatalogoSerieWritePort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SerieWriteRoutingServiceTest {

    @Test
    void deveUsarCatalogoComoOwnershipOficialQuandoNivelEnsinoForResolvido() {
        AtomicBoolean catalogCalled = new AtomicBoolean(false);
        CatalogoSerieWritePort catalog = (query, context, nivel, command) -> {
            catalogCalled.set(true);
            return Mono.just(resultado("ENSINO_FUNDAMENTAL", "catalog"));
        };
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) ->
                Mono.just(new NivelEnsinoResolved(UUID.randomUUID(), "ENSINO_FUNDAMENTAL"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                catalog, resolver, authContext, observability);

        StepVerifier.create(service.executar(query(), command("ENSINO_FUNDAMENTAL", null)))
                .assertNext(response -> assertThat(response.nivelEnsino()).isEqualTo("ENSINO_FUNDAMENTAL"))
                .verifyComplete();

        assertThat(catalogCalled.get()).isTrue();
    }

    @Test
    void deveRejeitarQuandoNivelEnsinoNaoVierInformado() {
        CatalogoSerieWritePort catalog = (query, context, nivel, command) -> Mono.just(resultado("ENSINO_FUNDAMENTAL", "catalog"));
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) ->
                Mono.just(new NivelEnsinoResolved(UUID.randomUUID(), "ENSINO_FUNDAMENTAL"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                catalog, resolver, authContext, observability);

        StepVerifier.create(service.executar(query(), command(" ", null)))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("nivelEnsino informado e obrigatorio"))
                .verify();
    }

    @Test
    void deveRejeitarQuandoNivelEnsinoNaoForResolvidoNoCatalogoOficial() {
        AtomicBoolean catalogCalled = new AtomicBoolean(false);
        CatalogoSerieWritePort catalog = (query, context, nivel, command) -> {
            catalogCalled.set(true);
            return Mono.just(resultado("ENSINO_FUNDAMENTAL", "catalog"));
        };
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) -> Mono.empty();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                catalog, resolver, authContext, observability);

        StepVerifier.create(service.executar(query(), command("ENSINO_MEDIO", null)))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("nivelEnsino informado nao foi encontrado"))
                .verify();

        assertThat(catalogCalled.get()).isFalse();
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        CatalogoSerieWritePort catalog = (query, context, nivel, command) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) ->
                Mono.just(new NivelEnsinoResolved(UUID.randomUUID(), "ENSINO_FUNDAMENTAL"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                catalog, resolver, authContext, observability);

        StepVerifier.create(service.executar(query(), command("ENSINO_FUNDAMENTAL", null)))
                .expectError(DownstreamUnavailableException.class)
                .verify();
    }

    @Test
    void deveRejeitarEscolaIdDiferenteDoContextoAutenticado() {
        UUID escolaContexto = UUID.randomUUID();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), escolaContexto, "Escola A"));
        CatalogoSerieWritePort catalog = (query, context, nivel, command) -> Mono.just(resultado("ENSINO_FUNDAMENTAL", "catalog"));
        CatalogoNivelEnsinoResolverPort resolver = (query, context, nivel) ->
                Mono.just(new NivelEnsinoResolved(UUID.randomUUID(), "ENSINO_FUNDAMENTAL"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        SerieWriteRoutingService service = new SerieWriteRoutingService(
                catalog, resolver, authContext, observability);

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
        @Override public void recordCatalogSuccess(CatalogWriteCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogWriteCutoverDecision decision, Throwable error) {}
    }
}
