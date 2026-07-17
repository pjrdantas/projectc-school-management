package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.LegacyDisciplinaWritePort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DisciplinaWriteRoutingServiceTest {

    @Test
    void deveUsarMonolitoQuandoCutoverNaoEstiverLiberado() {
        LegacyDisciplinaWritePort monolith = (query, command) -> Mono.just(resultado("ATIVA", "monolith"));
        CatalogoDisciplinaWritePort catalog = (query, context, command) -> Mono.just(resultado("ATIVA", "catalog"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, false, "cutover_disabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        DisciplinaWriteRoutingService service = new DisciplinaWriteRoutingService(
                monolith, catalog, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("ATIVA", null)))
                .assertNext(response -> assertThat(response.escolaNome()).isEqualTo("monolith"))
                .verifyComplete();
    }

    @Test
    void deveUsarMonolitoQuandoStatusNaoForCompativelComCatalogoNovo() {
        AtomicBoolean monolithCalled = new AtomicBoolean(false);
        AtomicBoolean catalogCalled = new AtomicBoolean(false);
        LegacyDisciplinaWritePort monolith = (query, command) -> {
            monolithCalled.set(true);
            return Mono.just(resultado("INATIVA", "monolith"));
        };
        CatalogoDisciplinaWritePort catalog = (query, context, command) -> {
            catalogCalled.set(true);
            return Mono.just(resultado("ATIVA", "catalog"));
        };
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        DisciplinaWriteRoutingService service = new DisciplinaWriteRoutingService(
                monolith, catalog, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("INATIVA", null)))
                .assertNext(response -> assertThat(response.status()).isEqualTo("INATIVA"))
                .verifyComplete();

        assertThat(monolithCalled.get()).isTrue();
        assertThat(catalogCalled.get()).isFalse();
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        AtomicBoolean monolithCalled = new AtomicBoolean(false);
        LegacyDisciplinaWritePort monolith = (query, command) -> {
            monolithCalled.set(true);
            return Mono.just(resultado("ATIVA", "monolith"));
        };
        CatalogoDisciplinaWritePort catalog = (query, context, command) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        DisciplinaWriteRoutingService service = new DisciplinaWriteRoutingService(
                monolith, catalog, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("ATIVA", null)))
                .expectError(DownstreamUnavailableException.class)
                .verify();

        assertThat(monolithCalled.get()).isFalse();
    }

    @Test
    void deveRejeitarEscolaIdDiferenteDoContextoAutenticado() {
        UUID escolaContexto = UUID.randomUUID();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), escolaContexto, "Escola A"));
        LegacyDisciplinaWritePort monolith = (query, command) -> Mono.just(resultado("ATIVA", "monolith"));
        CatalogoDisciplinaWritePort catalog = (query, context, command) -> Mono.just(resultado("ATIVA", "catalog"));
        CatalogWriteCutoverPolicyPort decider = route -> new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        DisciplinaWriteRoutingService service = new DisciplinaWriteRoutingService(
                monolith, catalog, authContext, decider, observability);

        StepVerifier.create(service.executar(query(), command("ATIVA", UUID.randomUUID())))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("escolaId informado diverge"))
                .verify();
    }

    private static CatalogWriteQuery query() {
        return new CatalogWriteQuery("Bearer token", "corr-1", "idem-1");
    }

    private static DisciplinaCreateCommand command(String status, UUID escolaId) {
        return new DisciplinaCreateCommand("Matematica", 80, status, escolaId);
    }

    private static DisciplinaCreatedResult resultado(String status, String escolaNome) {
        return new DisciplinaCreatedResult(
                UUID.randomUUID(),
                "Matematica",
                80,
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

