package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
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
class MatriculaWriteProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer ENROLLMENT_DOCUMENT = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.enrollment-document-service.base-url", () -> ENROLLMENT_DOCUMENT.url("/").toString());
        registry.add("clients.enrollment-document-service.internal-token", () -> "enrollment-document-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
        ENROLLMENT_DOCUMENT.shutdown();
    }

    @Test
    void deveCriarMatriculaNoServicoDonoComContratoPublicoCompativel() throws InterruptedException {
        enqueueContext();
        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"00000000-0000-0000-0000-000000000701","status":"PENDENTE"}
                        """));

        client.post().uri("/api/matriculas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000091",
                          "tipoMatricula":"PRIMEIRA_MATRICULA",
                          "observacao":"Matricula BFF"
                        }
                        """)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-matricula-write-create")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PENDENTE");

        assertThat(IDENTITY_ACCESS.takeRequest().getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var request = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/internal/v1/matriculas");
        assertThat(request.getHeader("X-Internal-Token")).isEqualTo("enrollment-document-internal-token");
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(request.getBody().readUtf8()).contains("\"alunoId\"").doesNotContain("\"serieId\"");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveAtualizarMatriculaNoServicoDonoSemFallbackLegado() throws InterruptedException {
        UUID matriculaId = UUID.fromString("00000000-0000-0000-0000-000000000701");
        enqueueContext();
        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"00000000-0000-0000-0000-000000000701","status":"PENDENTE"}
                        """));

        client.put().uri("/api/matriculas/{matriculaId}", matriculaId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "serieId":"00000000-0000-0000-0000-000000000081",
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000091",
                          "tipoMatricula":"PRIMEIRA_MATRICULA",
                          "dataMatricula":"2026-07-21",
                          "observacao":"Atualizada"
                        }
                        """)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-matricula-write-update")
                .exchange()
                .expectStatus().isOk();

        var request = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getPath()).isEqualTo("/internal/v1/matriculas/" + matriculaId);
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveAtualizarStatusNoServicoDonoComContratoPublicoCompativel() throws InterruptedException {
        UUID matriculaId = UUID.fromString("00000000-0000-0000-0000-000000000701");
        enqueueContext();
        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"00000000-0000-0000-0000-000000000701","status":"EFETIVADA"}
                        """));

        client.patch().uri("/api/matriculas/{matriculaId}/status", matriculaId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"status\":\"ATIVA\",\"justificativa\":\"Documentos conferidos\"}")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-matricula-write-status")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("EFETIVADA");

        var request = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getPath()).isEqualTo("/internal/v1/matriculas/" + matriculaId + "/status");
        assertThat(request.getBody().readUtf8())
                .contains("\"observacao\":\"Documentos conferidos\"")
                .doesNotContain("justificativa");
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveCancelarMatriculaNoServicoDonoPreservandoDeleteLegado() throws InterruptedException {
        UUID matriculaId = UUID.fromString("00000000-0000-0000-0000-000000000701");
        enqueueContext();
        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"00000000-0000-0000-0000-000000000701","status":"CANCELADA"}
                        """));

        client.delete().uri("/api/matriculas/{matriculaId}", matriculaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-matricula-write-cancel")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("CANCELADA");

        var request = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/internal/v1/matriculas/" + matriculaId + "/cancelamento");
        assertThat(request.getBody().readUtf8()).contains("Cancelamento solicitado no BFF");
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
