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
class DocumentoMatriculaWriteProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer ENROLLMENT_DOCUMENT = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.enrollment-document-service.base-url", () -> ENROLLMENT_DOCUMENT.url("/").toString());
        registry.add("clients.enrollment-document-service.internal-token", () -> "enrollment-document-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        ENROLLMENT_DOCUMENT.shutdown();
    }

    @Test
    void deveConsumirDocumentoMatriculaServiceNaCriacaoDeEscolaOrigem() throws InterruptedException {
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "id":"00000000-0000-0000-0000-000000000801",
                          "nomeEscola":"Escola Origem Integracao"
                        }
                        """));

        String requestBody = """
                {"nomeEscola":"Escola Origem Integracao"}
                """;

        client.post().uri("/api/escolas-origem")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-enrollment-write-3")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.nomeEscola").isEqualTo("Escola Origem Integracao");

        MONOLITH.takeRequest();
        var enrollmentRequest = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(enrollmentRequest.getPath()).isEqualTo("/internal/v1/escolas-origem");
        assertThat(enrollmentRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(enrollmentRequest.getHeader("X-Internal-Token")).isEqualTo("enrollment-document-internal-token");
        assertThat(enrollmentRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-enrollment-write-3");
        assertThat(enrollmentRequest.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveConsumirDocumentoMatriculaServiceNaCriacaoDeTransferencia() throws InterruptedException {
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "id":"00000000-0000-0000-0000-000000000901",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "tipoTransferencia":"ENTRADA"
                        }
                        """));

        String requestBody = """
                {"alunoId":"00000000-0000-0000-0000-000000000021","serieOrigem":"5A","anoLetivoOrigem":"2026","tipoTransferencia":"ENTRADA"}
                """;

        client.post().uri("/api/transferencias")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-enrollment-write-4")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.tipoTransferencia").isEqualTo("ENTRADA");

        MONOLITH.takeRequest();
        var enrollmentRequest = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(enrollmentRequest.getPath()).isEqualTo("/internal/v1/transferencias");
        assertThat(enrollmentRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(enrollmentRequest.getHeader("X-Internal-Token")).isEqualTo("enrollment-document-internal-token");
        assertThat(enrollmentRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-enrollment-write-4");
        assertThat(enrollmentRequest.getBody().readUtf8()).isEqualTo(requestBody);
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

