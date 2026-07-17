package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.SessaoAutenticadaPort;
import br.com.escola.bff.application.port.out.TenantAtivoReadPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class IdentityTenantContextFallbackObservabilityTest {

    @Test
    void deveRegistrarIdentityAccessQuandoFalhaDeListagemOcorrerNaResolucaoDeContexto() {
        IdentityTenantAuthContextPort authContextPort = query -> Mono.error(
                new DownstreamUnavailableException("identity indisponivel"));
        TenantAtivoReadPort institutionalPort = new NoOpTenantAtivoReadPort();
        RecordingObservability observability = new RecordingObservability();

        AuthSessionProxyService service = new AuthSessionProxyService(
                authContextPort,
                new NoOpSessaoAutenticadaPort(),
                institutionalPort,
                observability);

        StepVerifier.create(service.listarEscolas("Bearer token", "corr-ctx-1"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(DownstreamUnavailableException.class);
                    assertThat(error).hasMessage("identity indisponivel");
                })
                .verify();

        assertThat(observability.failureTarget).isNull();
        assertThat(observability.fallbackTarget).isNull();
    }

    @Test
    void deveRegistrarIdentityAccessQuandoFalhaDeTenantAtivoOcorrerNaResolucaoDeContexto() {
        IdentityTenantAuthContextPort authContextPort = query -> Mono.error(
                new DownstreamUnavailableException("identity indisponivel"));
        RecordingObservability observability = new RecordingObservability();

        TenantAtivoReadProxyService service = new TenantAtivoReadProxyService(
                authContextPort,
                new NoOpTenantAtivoReadPort(),
                observability);

        StepVerifier.create(service.consultarTenantAtivo("Bearer token", "corr-ctx-2"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(DownstreamUnavailableException.class);
                    assertThat(error).hasMessage("identity indisponivel");
                })
                .verify();

        assertThat(observability.failureTarget).isNull();
        assertThat(observability.fallbackTarget).isNull();
    }

    private static final class RecordingObservability implements IdentityTenantObservabilityPort {

        private String failureTarget;
        private String fallbackTarget;

        @Override
        public void recordDirectLegacy(IdentityTenantCutoverDecision decision) {
        }

        @Override
        public void recordServiceSuccess(IdentityTenantCutoverDecision decision, String target) {
        }

        @Override
        public void recordServiceFailure(IdentityTenantCutoverDecision decision, String target, Throwable error) {
            this.failureTarget = target;
        }

        @Override
        public void recordFallbackToLegacy(IdentityTenantCutoverDecision decision, String target, Throwable error) {
            this.fallbackTarget = target;
        }
    }

    private static final class NoOpTenantAtivoReadPort implements TenantAtivoReadPort {

        @Override
        public Mono<ResponseEntity<String>> listarEscolasDisponiveis(CatalogReadQuery query, AuthSessionContext context) {
            return Mono.just(ResponseEntity.ok("institutional-schools"));
        }

        @Override
        public Mono<ResponseEntity<String>> consultarTenantAtivo(CatalogReadQuery query, AuthSessionContext context) {
            return Mono.just(ResponseEntity.ok("institutional-tenant"));
        }
    }

    private static final class NoOpSessaoAutenticadaPort implements SessaoAutenticadaPort {

        @Override
        public Mono<ResponseEntity<String>> listarEscolas(CatalogReadQuery query, AuthSessionContext context) {
            return Mono.just(ResponseEntity.ok("unused"));
        }

        @Override
        public Mono<ResponseEntity<String>> selecionarEscolaAtiva(
                String requestBody,
                CatalogReadQuery query,
                AuthSessionContext context) {
            return Mono.just(ResponseEntity.ok("unused"));
        }
    }
}

