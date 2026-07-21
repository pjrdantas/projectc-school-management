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
class ResponsavelWriteProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer RESPONSIBLES = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.responsibles-service.base-url", () -> RESPONSIBLES.url("/").toString());
        registry.add("clients.responsibles-service.internal-token", () -> "responsibles-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
        RESPONSIBLES.shutdown();
    }

    @Test
    void deveCriarResponsavelNoServicoDonoSemFallbackLegado() throws InterruptedException {
        enqueueContext();
        RESPONSIBLES.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"00000000-0000-0000-0000-000000000601","nomeCompleto":"Maria Souza"}
                        """));

        client.post().uri("/api/responsaveis")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"nomeCompleto":"Maria Souza","cpf":"12345678901","escolaId":"00000000-0000-0000-0000-000000000047"}
                        """)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-write-create")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.nomeCompleto").isEqualTo("Maria Souza");

        assertThat(IDENTITY_ACCESS.takeRequest().getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var request = RESPONSIBLES.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/internal/v1/responsaveis");
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(request.getBody().readUtf8()).contains("\"escolaId\"");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveAtualizarResponsavelNoServicoDono() throws InterruptedException {
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        enqueueContext();
        RESPONSIBLES.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"00000000-0000-0000-0000-000000000601","nomeCompleto":"Maria Atualizada"}
                        """));

        client.put().uri("/api/responsaveis/{id}", responsavelId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nomeCompleto\":\"Maria Atualizada\",\"cpf\":\"12345678901\"}")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-write-update")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nomeCompleto").isEqualTo("Maria Atualizada");

        var request = RESPONSIBLES.takeRequest();
        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getPath()).isEqualTo("/internal/v1/responsaveis/" + responsavelId);
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveExcluirResponsavelNoServicoDono() throws InterruptedException {
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        enqueueContext();
        RESPONSIBLES.enqueue(new MockResponse().setResponseCode(204));

        client.delete().uri("/api/responsaveis/{id}", responsavelId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-write-delete")
                .exchange()
                .expectStatus().isNoContent();

        var request = RESPONSIBLES.takeRequest();
        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getPath()).isEqualTo("/internal/v1/responsaveis/" + responsavelId);
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    private void enqueueContext() {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));
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
