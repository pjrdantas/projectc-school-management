package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CatalogReadRoutingServiceTest {

    @Test
    void deveUsarMonolitoQuandoCutoverNaoEstiverLiberado() {
        MonolithCatalogReadPort monolith = (path, query) -> Mono.just(ResponseEntity.ok("monolith"));
        AcademicCatalogReadPort catalog = (path, query, context) -> Mono.just(ResponseEntity.ok("catalog"));
        InternalAuthContextPort authContext = query -> Mono.just(new AuthSessionContext(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        CatalogReadCutoverPolicyPort decider = new FixedDecider(false, true);
        CatalogReadObservabilityPort observability = new NoOpObservability();

        CatalogReadRoutingService service = new CatalogReadRoutingService(monolith, catalog, authContext, decider, observability);

        StepVerifier.create(service.executar(CatalogReadRoute.DISCIPLINAS, new CatalogReadQuery("Bearer token", "corr-1")))
                .assertNext(response -> assertThat(response.getBody()).isEqualTo("monolith"))
                .verifyComplete();
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        AtomicBoolean fallbackHit = new AtomicBoolean(false);
        MonolithCatalogReadPort monolith = (path, query) -> {
            fallbackHit.set(true);
            return Mono.just(ResponseEntity.ok("fallback"));
        };
        AcademicCatalogReadPort catalog = (path, query, context) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        InternalAuthContextPort authContext = query -> Mono.just(new AuthSessionContext(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        CatalogReadCutoverPolicyPort decider = new FixedDecider(true, true);
        CatalogReadObservabilityPort observability = new NoOpObservability();

        CatalogReadRoutingService service = new CatalogReadRoutingService(monolith, catalog, authContext, decider, observability);

        StepVerifier.create(service.executar(CatalogReadRoute.DISCIPLINAS, new CatalogReadQuery("Bearer token", "corr-1")))
                .assertNext(response -> assertThat(response.getBody()).isEqualTo("fallback"))
                .verifyComplete();

        assertThat(fallbackHit.get()).isTrue();
    }

    private record FixedDecider(boolean useCatalog, boolean fallback) implements CatalogReadCutoverPolicyPort {

        @Override public CatalogReadCutoverDecision decision(CatalogReadRoute route) {
            return new CatalogReadCutoverDecision(route, useCatalog, useCatalog ? "catalog_enabled" : "cutover_disabled");
        }

        @Override public boolean fallbackToMonolithOnError() { return fallback; }
    }

    private static final class NoOpObservability implements CatalogReadObservabilityPort {
        @Override public void recordDirectMonolith(CatalogReadCutoverDecision decision) {}
        @Override public void recordCatalogSuccess(CatalogReadCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogReadCutoverDecision decision, Throwable error) {}
        @Override public void recordFallbackToMonolith(CatalogReadCutoverDecision decision, Throwable error) {}
    }
}
