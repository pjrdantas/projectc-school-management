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
class AuthSessionProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
    }

    @Test
    void deveConsumirIdentityAccessServiceNaListagemOficialDeEscolasDaSessao() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setHeader(TrustedHeaders.CORRELATION_ID, "corr-auth-1")
                .setBody("""
                        [
                          {
                            "escolaId":"00000000-0000-0000-0000-000000000047",
                            "escolaNome":"Escola padrao",
                            "ativa":true
                          }
                        ]
                        """));

        client.get().uri("/api/auth/escolas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-auth-1")
                .expectBody()
                .jsonPath("$[0].ativa").isEqualTo(true);

        var contextRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(contextRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(contextRequest.getHeader("X-Internal-Token")).isEqualTo("identity-access-internal-token");
        assertThat(contextRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-auth-1");

        var identityRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(identityRequest.getPath()).isEqualTo("/internal/v1/auth/escolas");
        assertThat(identityRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(identityRequest.getHeader("X-Internal-Token")).isEqualTo("identity-access-internal-token");
        assertThat(identityRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-auth-1");
        assertThat(identityRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(identityRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveConsumirIdentityAccessServiceNaTrocaOficialDeEscolaAtiva() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000099",
                          "escolaNome":"Escola Secundaria",
                          "username":"usuario.teste"
                        }
                        """));

        client.post().uri("/api/auth/escola-ativa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-2")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "escolaId":"00000000-0000-0000-0000-000000000099"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.escolaId").isEqualTo("00000000-0000-0000-0000-000000000099")
                .jsonPath("$.escolaNome").isEqualTo("Escola Secundaria");

        var contextRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var identityRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(identityRequest.getPath()).isEqualTo("/internal/v1/auth/escola-ativa");
        assertThat(identityRequest.getMethod()).isEqualTo("POST");
        assertThat(identityRequest.getBody().readUtf8()).contains("00000000-0000-0000-0000-000000000099");
        assertThat(identityRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(identityRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveExigirBearerTokenNasRotasOficiaisDeSessaoMultiescola() {
        client.get().uri("/api/auth/escolas")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-3")
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
