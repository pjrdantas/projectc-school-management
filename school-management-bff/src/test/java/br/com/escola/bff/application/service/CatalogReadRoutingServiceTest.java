package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoReadPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyCatalogReadPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CatalogReadRoutingServiceTest {

    @Test
    void deveUsarMonolitoQuandoCutoverNaoEstiverLiberado() {
        LegacyCatalogReadPort monolith = (path, query) -> Mono.just(ResponseEntity.ok("monolith"));
        CatalogoReadPort catalog = (path, query, context) -> Mono.just(ResponseEntity.ok("catalog"));
        InternalAuthContextPort authContext = query -> Mono.just(new AuthSessionContext(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        CatalogReadCutoverPolicyPort decider = new FixedDecider(false, true);
        CatalogReadObservabilityPort observability = new NoOpObservability();

        CatalogReadRoutingService service = new CatalogReadRoutingService(monolith, catalog, authContext, decider, observability);

        StepVerifier.create(service.executar(CatalogReadRoute.DISCIPLINAS, new CatalogReadQuery("Bearer token", "corr-1")))
                .assertNext(response -> assertThat(response.getBody()).isEqualTo("monolith"))
                .verifyComplete();
    }

    @Test
    void devePropagarErroQuandoCatalogoFalhar() {
        AtomicBoolean fallbackHit = new AtomicBoolean(false);
        LegacyCatalogReadPort monolith = (path, query) -> {
            fallbackHit.set(true);
            return Mono.just(ResponseEntity.ok("fallback"));
        };
        CatalogoReadPort catalog = (path, query, context) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        InternalAuthContextPort authContext = query -> Mono.just(new AuthSessionContext(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        CatalogReadCutoverPolicyPort decider = new FixedDecider(true, true);
        CatalogReadObservabilityPort observability = new NoOpObservability();

        CatalogReadRoutingService service = new CatalogReadRoutingService(monolith, catalog, authContext, decider, observability);

        StepVerifier.create(service.executar(CatalogReadRoute.DISCIPLINAS, new CatalogReadQuery("Bearer token", "corr-1")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(DownstreamUnavailableException.class);
                    assertThat(error).hasMessage("catalog indisponivel");
                })
                .verify();

        assertThat(fallbackHit.get()).isFalse();
    }

    private record FixedDecider(boolean useCatalog, boolean fallback) implements CatalogReadCutoverPolicyPort {

        @Override public CatalogReadCutoverDecision decision(CatalogReadRoute route) {
            return new CatalogReadCutoverDecision(route, useCatalog, useCatalog ? "catalog_enabled" : "cutover_disabled");
        }

        @Override public boolean fallbackToLegacyOnError() { return fallback; }
    }

    private static final class NoOpObservability implements CatalogReadObservabilityPort {
        @Override public void recordDirectLegacy(CatalogReadCutoverDecision decision) {}
        @Override public void recordCatalogSuccess(CatalogReadCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogReadCutoverDecision decision, Throwable error) {}
        @Override public void recordFallbackToLegacy(CatalogReadCutoverDecision decision, Throwable error) {}
    }
}

