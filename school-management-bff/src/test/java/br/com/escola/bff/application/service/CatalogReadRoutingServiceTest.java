package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CatalogReadRoutingServiceTest {

    @Test
    void deveUsarMonolitoQuandoCutoverNaoEstiverLiberado() {
        MonolithCatalogReadPort monolith = (path, query) -> Mono.just(ResponseEntity.ok("monolith"));
        AcademicCatalogReadPort catalog = (path, query, context) -> Mono.just(ResponseEntity.ok("catalog"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        CatalogReadCutoverPolicyPort decider = new FixedDecider(false, true);

        CatalogReadRoutingService service = new CatalogReadRoutingService(monolith, catalog, authContext, decider);

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
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        CatalogReadCutoverPolicyPort decider = new FixedDecider(true, true);

        CatalogReadRoutingService service = new CatalogReadRoutingService(monolith, catalog, authContext, decider);

        StepVerifier.create(service.executar(CatalogReadRoute.DISCIPLINAS, new CatalogReadQuery("Bearer token", "corr-1")))
                .assertNext(response -> assertThat(response.getBody()).isEqualTo("fallback"))
                .verifyComplete();

        assertThat(fallbackHit.get()).isTrue();
    }

    private record FixedDecider(boolean useCatalog, boolean fallback) implements CatalogReadCutoverPolicyPort {

        @Override public boolean shouldUseCatalog(CatalogReadRoute route) { return useCatalog; }

        @Override public boolean fallbackToMonolithOnError() { return fallback; }
    }
}
