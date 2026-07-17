package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoReadPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CatalogReadRoutingServiceTest {

    @Test
    void deveUsarCatalogoQuandoLeituraOficialForExecutada() {
        CatalogoReadPort catalog = (path, query, context) -> Mono.just(ResponseEntity.ok("catalog"));
        InternalAuthContextPort authContext = query -> Mono.just(new AuthSessionContext(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        CatalogReadObservabilityPort observability = new NoOpObservability();

        CatalogReadRoutingService service = new CatalogReadRoutingService(catalog, authContext, observability);

        StepVerifier.create(service.executar(CatalogReadRoute.DISCIPLINAS, new CatalogReadQuery("Bearer token", "corr-1")))
                .assertNext(response -> assertThat(response.getBody()).isEqualTo("catalog"))
                .verifyComplete();
    }

    @Test
    void devePropagarErroQuandoCatalogoFalhar() {
        AtomicBoolean fallbackHit = new AtomicBoolean(false);
        CatalogoReadPort catalog = (path, query, context) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        InternalAuthContextPort authContext = query -> Mono.just(new AuthSessionContext(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        CatalogReadObservabilityPort observability = new NoOpObservability();

        CatalogReadRoutingService service = new CatalogReadRoutingService(catalog, authContext, observability);

        StepVerifier.create(service.executar(CatalogReadRoute.DISCIPLINAS, new CatalogReadQuery("Bearer token", "corr-1")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(DownstreamUnavailableException.class);
                    assertThat(error).hasMessage("catalog indisponivel");
                })
                .verify();

        assertThat(fallbackHit.get()).isFalse();
    }

    private static final class NoOpObservability implements CatalogReadObservabilityPort {
        @Override public void recordDirectLegacy(CatalogReadCutoverDecision decision) {}
        @Override public void recordCatalogSuccess(CatalogReadCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogReadCutoverDecision decision, Throwable error) {}
        @Override public void recordFallbackToLegacy(CatalogReadCutoverDecision decision, Throwable error) {}
    }
}

