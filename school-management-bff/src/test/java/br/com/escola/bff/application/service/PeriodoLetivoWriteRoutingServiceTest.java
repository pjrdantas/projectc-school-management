package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.LegacyPeriodoLetivoWritePort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PeriodoLetivoWriteRoutingServiceTest {

    @Test
    void deveUsarMonolitoQuandoCutoverNaoEstiverLiberado() {
        LegacyPeriodoLetivoWritePort monolith = (query, command) -> Mono.just(resultado("monolith"));
        CatalogoPeriodoLetivoWritePort catalog = (query, context, command) -> Mono.just(resultado("catalog"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, false, "cutover_disabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        PeriodoLetivoWriteRoutingService service = new PeriodoLetivoWriteRoutingService(
                monolith,
                catalog,
                authContext,
                decider,
                observability);

        StepVerifier.create(service.executar(query(), command(null)))
                .assertNext(response -> assertThat(response.escolaNome()).isEqualTo("monolith"))
                .verifyComplete();
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        AtomicBoolean monolithCalled = new AtomicBoolean(false);
        LegacyPeriodoLetivoWritePort monolith = (query, command) -> {
            monolithCalled.set(true);
            return Mono.just(resultado("monolith"));
        };
        CatalogoPeriodoLetivoWritePort catalog = (query, context, command) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        PeriodoLetivoWriteRoutingService service = new PeriodoLetivoWriteRoutingService(
                monolith,
                catalog,
                authContext,
                decider,
                observability);

        StepVerifier.create(service.executar(query(), command(null)))
                .expectError(DownstreamUnavailableException.class)
                .verify();

        assertThat(monolithCalled.get()).isFalse();
    }

    @Test
    void deveRejeitarEscolaIdDiferenteDoContextoAutenticado() {
        UUID escolaContexto = UUID.randomUUID();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), escolaContexto, "Escola A"));
        LegacyPeriodoLetivoWritePort monolith = (query, command) -> Mono.just(resultado("monolith"));
        CatalogoPeriodoLetivoWritePort catalog = (query, context, command) -> Mono.just(resultado("catalog"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        PeriodoLetivoWriteRoutingService service = new PeriodoLetivoWriteRoutingService(
                monolith,
                catalog,
                authContext,
                decider,
                observability);

        StepVerifier.create(service.executar(query(), command(UUID.randomUUID())))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("escolaId informado diverge"))
                .verify();
    }

    private static CatalogWriteQuery query() {
        return new CatalogWriteQuery("Bearer token", "corr-1", "idem-1");
    }

    private static PeriodoLetivoCreateCommand command(UUID escolaId) {
        return new PeriodoLetivoCreateCommand(
                "2026",
                2026,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 12, 20),
                escolaId);
    }

    private static PeriodoLetivoCreatedResult resultado(String escolaNome) {
        return new PeriodoLetivoCreatedResult(
                UUID.randomUUID(),
                "2026",
                2026,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 12, 20),
                true,
                UUID.randomUUID(),
                escolaNome,
                java.time.LocalDateTime.now());
    }

    private static final class NoOpObservability implements CatalogWriteObservabilityPort {
        @Override public void recordDirectLegacy(CatalogWriteCutoverDecision decision) {}
        @Override public void recordCatalogSuccess(CatalogWriteCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogWriteCutoverDecision decision, Throwable error) {}
    }
}

