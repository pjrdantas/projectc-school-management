package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class InstitutionalTenantReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer INSTITUTIONAL_TENANT = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.institutional-tenant-service.base-url", () -> INSTITUTIONAL_TENANT.url("/").toString());
        registry.add("clients.institutional-tenant-service.internal-token", () -> "institutional-tenant-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        INSTITUTIONAL_TENANT.shutdown();
    }

    @Test
    void deveConsumirInstitutionalTenantServiceNaLeituraOficialDoTenantAtivo() throws InterruptedException {
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        INSTITUTIONAL_TENANT.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setHeader(TrustedHeaders.CORRELATION_ID, "corr-tenant-1")
                .setBody("""
                        {
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        client.get().uri("/api/auth/tenant/ativa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-tenant-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-tenant-1")
                .expectBody()
                .jsonPath("$.escolaId").isEqualTo("00000000-0000-0000-0000-000000000047")
                .jsonPath("$.escolaNome").isEqualTo("Escola padrao");

        var authRequest = MONOLITH.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/api/auth/contexto-atual");

        var tenantRequest = INSTITUTIONAL_TENANT.takeRequest();
        assertThat(tenantRequest.getPath()).isEqualTo("/internal/v1/tenant/ativa");
        assertThat(tenantRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(tenantRequest.getHeader("X-Internal-Token")).isEqualTo("institutional-tenant-internal-token");
        assertThat(tenantRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-tenant-1");
        assertThat(tenantRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(tenantRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveExigirBearerTokenNaLeituraOficialDoTenantAtivo() {
        client.get().uri("/api/auth/tenant/ativa")
                .header(TrustedHeaders.CORRELATION_ID, "corr-tenant-2")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED");
    }

    private static MockWebServer startServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
