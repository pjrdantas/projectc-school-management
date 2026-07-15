package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.IdentityAccessSessionPort;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.IdentityTenantCutoverPolicyPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.InstitutionalTenantReadPort;
import br.com.escola.bff.application.port.out.MonolithAuthSessionPort;
import br.com.escola.bff.application.port.out.MonolithTenantReadPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class IdentityTenantContextFallbackObservabilityTest {

    @Test
    void deveRegistrarIdentityAccessQuandoFallbackDeListagemFalharNaResolucaoDeContexto() {
        IdentityTenantAuthContextPort authContextPort = query -> Mono.error(
                new DownstreamUnavailableException("identity indisponivel"));
        InstitutionalTenantReadPort institutionalPort = new NoOpInstitutionalTenantReadPort();
        MonolithAuthSessionPort monolithPort = new StubMonolithAuthSessionPort();
        RecordingObservability observability = new RecordingObservability();

        AuthSessionProxyService service = new AuthSessionProxyService(
                authContextPort,
                new NoOpIdentityAccessSessionPort(),
                institutionalPort,
                monolithPort,
                new FixedDecisionPolicy(IdentityTenantRoute.AUTH_ESCOLAS),
                observability);

        StepVerifier.create(service.listarEscolas("Bearer token", "corr-ctx-1"))
                .assertNext(response -> assertThat(response.getBody()).isEqualTo("monolith-auth"))
                .verifyComplete();

        assertThat(observability.failureTarget).isEqualTo("identity_access");
        assertThat(observability.fallbackTarget).isEqualTo("identity_access");
    }

    @Test
    void deveRegistrarIdentityAccessQuandoFallbackDeTenantAtivoFalharNaResolucaoDeContexto() {
        IdentityTenantAuthContextPort authContextPort = query -> Mono.error(
                new DownstreamUnavailableException("identity indisponivel"));
        MonolithTenantReadPort monolithPort = query -> Mono.just(ResponseEntity.ok("monolith-tenant"));
        RecordingObservability observability = new RecordingObservability();

        InstitutionalTenantReadProxyService service = new InstitutionalTenantReadProxyService(
                authContextPort,
                new NoOpInstitutionalTenantReadPort(),
                monolithPort,
                new FixedDecisionPolicy(IdentityTenantRoute.AUTH_TENANT_ATIVA),
                observability);

        StepVerifier.create(service.consultarTenantAtivo("Bearer token", "corr-ctx-2"))
                .assertNext(response -> assertThat(response.getBody()).isEqualTo("monolith-tenant"))
                .verifyComplete();

        assertThat(observability.failureTarget).isEqualTo("identity_access");
        assertThat(observability.fallbackTarget).isEqualTo("identity_access");
    }

    private static final class FixedDecisionPolicy implements IdentityTenantCutoverPolicyPort {

        private final IdentityTenantRoute route;

        private FixedDecisionPolicy(IdentityTenantRoute route) {
            this.route = route;
        }

        @Override
        public IdentityTenantCutoverDecision decision(IdentityTenantRoute ignored) {
            return new IdentityTenantCutoverDecision(route, true, "test");
        }

        @Override
        public boolean fallbackToMonolithOnError() {
            return true;
        }
    }

    private static final class RecordingObservability implements IdentityTenantObservabilityPort {

        private String failureTarget;
        private String fallbackTarget;

        @Override
        public void recordDirectMonolith(IdentityTenantCutoverDecision decision) {
        }

        @Override
        public void recordServiceSuccess(IdentityTenantCutoverDecision decision, String target) {
        }

        @Override
        public void recordServiceFailure(IdentityTenantCutoverDecision decision, String target, Throwable error) {
            this.failureTarget = target;
        }

        @Override
        public void recordFallbackToMonolith(IdentityTenantCutoverDecision decision, String target, Throwable error) {
            this.fallbackTarget = target;
        }
    }

    private static final class NoOpInstitutionalTenantReadPort implements InstitutionalTenantReadPort {

        @Override
        public Mono<ResponseEntity<String>> listarEscolasDisponiveis(CatalogReadQuery query, AuthSessionContext context) {
            return Mono.just(ResponseEntity.ok("institutional-schools"));
        }

        @Override
        public Mono<ResponseEntity<String>> consultarTenantAtivo(CatalogReadQuery query, AuthSessionContext context) {
            return Mono.just(ResponseEntity.ok("institutional-tenant"));
        }
    }

    private static final class StubMonolithAuthSessionPort implements MonolithAuthSessionPort {

        @Override
        public Mono<ResponseEntity<String>> listarEscolas(CatalogReadQuery query) {
            return Mono.just(ResponseEntity.ok("monolith-auth"));
        }

        @Override
        public Mono<ResponseEntity<String>> selecionarEscolaAtiva(String requestBody, CatalogReadQuery query) {
            return Mono.just(ResponseEntity.ok("unused"));
        }
    }

    private static final class NoOpIdentityAccessSessionPort implements IdentityAccessSessionPort {

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
