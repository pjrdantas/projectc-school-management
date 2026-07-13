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
class AuthSessionFallbackIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("features.identity-tenant-cutover.enabled", () -> true);
        registry.add("features.identity-tenant-cutover.fallback-to-monolith-on-error", () -> true);
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoIdentityAccessFalharNaTrocaDeEscolaAtiva() throws InterruptedException {
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));
        IDENTITY_ACCESS.enqueue(new MockResponse().setResponseCode(503));
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000099",
                          "escolaNome":"Escola monolito fallback",
                          "username":"usuario.teste"
                        }
                        """));

        client.post().uri("/api/auth/escola-ativa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-fallback")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "escolaId":"00000000-0000-0000-0000-000000000099"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.escolaNome").isEqualTo("Escola monolito fallback");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/api/auth/contexto-atual");

        var identityRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(identityRequest.getPath()).isEqualTo("/internal/v1/auth/escola-ativa");

        var monolithFallback = MONOLITH.takeRequest();
        assertThat(monolithFallback.getPath()).isEqualTo("/internal/auth/escola-ativa");
        assertThat(monolithFallback.getBody().readUtf8()).contains("00000000-0000-0000-0000-000000000099");
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
