package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

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
class AdministracaoAcessoProxyIntegrationTest {

    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer MONOLITH = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        IDENTITY_ACCESS.shutdown();
        MONOLITH.shutdown();
    }

    @Test
    void deveListarUsuariosPeloServicoOficial() throws InterruptedException {
        enqueueContext();
        IDENTITY_ACCESS.enqueue(jsonResponse("[{\"id\":\"" + USUARIO_ID + "\",\"username\":\"admin\"}]"));

        client.get().uri("/api/usuarios")
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-access-list")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].username").isEqualTo("admin");

        assertContextRequest("corr-access-list");
        var request = IDENTITY_ACCESS.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/internal/v1/usuarios");
        assertTrustedHeaders(request, "corr-access-list");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveCriarPerfilPeloServicoOficial() throws InterruptedException {
        enqueueContext();
        IDENTITY_ACCESS.enqueue(jsonResponse("{\"id\":\"" + UUID.randomUUID() + "\",\"codigo\":\"SECRETARIA\"}")
                .setResponseCode(201));

        client.post().uri("/api/perfis")
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-access-create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"codigo\":\"SECRETARIA\",\"nome\":\"Secretaria\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("SECRETARIA");

        assertContextRequest("corr-access-create");
        var request = IDENTITY_ACCESS.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/internal/v1/perfis");
        assertThat(request.getBody().readUtf8()).contains("SECRETARIA");
        assertTrustedHeaders(request, "corr-access-create");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveExcluirPermissaoPeloServicoOficial() throws InterruptedException {
        UUID permissaoId = UUID.randomUUID();
        enqueueContext();
        IDENTITY_ACCESS.enqueue(new MockResponse().setResponseCode(204));

        client.delete().uri("/api/permissoes/{id}", permissaoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-access-delete")
                .exchange()
                .expectStatus().isNoContent();

        assertContextRequest("corr-access-delete");
        var request = IDENTITY_ACCESS.takeRequest();
        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getPath()).isEqualTo("/internal/v1/permissoes/" + permissaoId);
        assertTrustedHeaders(request, "corr-access-delete");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveExigirBearerNasRotasAdministrativas() {
        client.get().uri("/api/usuarios")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED");
    }

    private void enqueueContext() {
        IDENTITY_ACCESS.enqueue(jsonResponse("""
                {
                  "usuarioId":"%s",
                  "escolaId":"%s",
                  "escolaNome":"Escola padrao"
                }
                """.formatted(USUARIO_ID, ESCOLA_ID)));
    }

    private void assertContextRequest(String correlationId) throws InterruptedException {
        var request = IDENTITY_ACCESS.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer access-token");
        assertThat(request.getHeader("X-Internal-Token")).isEqualTo("identity-access-internal-token");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo(correlationId);
    }

    private void assertTrustedHeaders(okhttp3.mockwebserver.RecordedRequest request, String correlationId) {
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer access-token");
        assertThat(request.getHeader("X-Internal-Token")).isEqualTo("identity-access-internal-token");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo(correlationId);
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo(USUARIO_ID.toString());
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo(ESCOLA_ID.toString());
    }

    private static MockResponse jsonResponse(String body) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
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
