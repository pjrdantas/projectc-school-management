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
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogoTurmaWritePort;
import br.com.escola.bff.application.port.out.CatalogoTurnoResolverPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TurmaWriteRoutingServiceTest {

    @Test
    void deveUsarCatalogoComoOwnershipOficialQuandoTurnoForResolvido() {
        AtomicBoolean catalogCalled = new AtomicBoolean(false);
        CatalogoTurmaWritePort catalog = (query, context, turno, command) -> {
            catalogCalled.set(true);
            return Mono.just(resultado("MANHA", "ATIVA", "catalog"));
        };
        CatalogoTurnoResolverPort resolver = (query, context, turno) ->
                Mono.just(new TurnoResolved(UUID.randomUUID(), "MANHA"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                catalog, resolver, authContext, observability);

        StepVerifier.create(service.executar(query(), command("MANHA", "ATIVA", null)))
                .assertNext(response -> assertThat(response.turno()).isEqualTo("MANHA"))
                .verifyComplete();

        assertThat(catalogCalled.get()).isTrue();
    }

    @Test
    void deveRejeitarQuandoStatusNaoForCompativelComCatalogoOficial() {
        CatalogoTurmaWritePort catalog = (query, context, turno, command) ->
                Mono.just(resultado("MANHA", "ATIVA", "catalog"));
        CatalogoTurnoResolverPort resolver = (query, context, turno) ->
                Mono.just(new TurnoResolved(UUID.randomUUID(), "MANHA"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                catalog, resolver, authContext, observability);

        StepVerifier.create(service.executar(query(), command("MANHA", "INATIVA", null)))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("status informado deve ser ATIVA"))
                .verify();
    }

    @Test
    void deveRejeitarQuandoTurnoNaoForResolvidoNoCatalogoOficial() {
        AtomicBoolean catalogCalled = new AtomicBoolean(false);
        CatalogoTurmaWritePort catalog = (query, context, turno, command) -> {
            catalogCalled.set(true);
            return Mono.just(resultado("MANHA", "ATIVA", "catalog"));
        };
        CatalogoTurnoResolverPort resolver = (query, context, turno) -> Mono.empty();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                catalog, resolver, authContext, observability);

        StepVerifier.create(service.executar(query(), command("NOITE", "ATIVA", null)))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("turno informado nao foi encontrado"))
                .verify();

        assertThat(catalogCalled.get()).isFalse();
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        CatalogoTurmaWritePort catalog = (query, context, turno, command) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        CatalogoTurnoResolverPort resolver = (query, context, turno) ->
                Mono.just(new TurnoResolved(UUID.randomUUID(), "MANHA"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                catalog, resolver, authContext, observability);

        StepVerifier.create(service.executar(query(), command("MANHA", "ATIVA", null)))
                .expectError(DownstreamUnavailableException.class)
                .verify();
    }

    @Test
    void deveRejeitarEscolaIdDiferenteDoContextoAutenticado() {
        UUID escolaContexto = UUID.randomUUID();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), escolaContexto, "Escola A"));
        CatalogoTurmaWritePort catalog = (query, context, turno, command) ->
                Mono.just(resultado("MANHA", "ATIVA", "catalog"));
        CatalogoTurnoResolverPort resolver = (query, context, turno) ->
                Mono.just(new TurnoResolved(UUID.randomUUID(), "MANHA"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaWriteRoutingService service = new TurmaWriteRoutingService(
                catalog, resolver, authContext, observability);

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
        @Override public void recordCatalogSuccess(CatalogWriteCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogWriteCutoverDecision decision, Throwable error) {}
    }
}
