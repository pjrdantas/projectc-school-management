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
class DocumentoAlunoReadProxyIntegrationTest {

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
    void deveConsumirEnrollmentDocumentServiceNaListagemOficialDeDocumentosPorAluno() throws InterruptedException {
        String alunoId = "00000000-0000-0000-0000-000000000021";

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
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000301",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "tipoDocumento":"HISTORICO_ESCOLAR",
                          "nomeArquivo":"historico.pdf",
                          "urlArquivo":"s3://bucket/historico.pdf",
                          "numeroDocumento":"historico.pdf",
                          "caminhoArquivo":"s3://bucket/historico.pdf",
                          "dataUpload":"2026-07-12T10:00:00",
                          "observacao":"Documento escolar"
                        }]
                        """));

        client.get().uri("/api/documentos-alunos/alunos/{alunoId}", alunoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-doc-aluno-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].alunoId").isEqualTo(alunoId)
                .jsonPath("$[0].tipoDocumento").isEqualTo("HISTORICO_ESCOLAR");

        MONOLITH.takeRequest();
        var enrollmentRequest = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(enrollmentRequest.getPath()).isEqualTo("/internal/v1/documentos-alunos/alunos/" + alunoId);
        assertThat(enrollmentRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(enrollmentRequest.getHeader("X-Internal-Token")).isEqualTo("enrollment-document-internal-token");
        assertThat(enrollmentRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-doc-aluno-2");
        assertThat(enrollmentRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(enrollmentRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveConsumirEnrollmentDocumentServiceNaBuscaOficialDeDocumentoPorId() throws InterruptedException {
        String documentoId = "00000000-0000-0000-0000-000000000301";

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
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "id":"00000000-0000-0000-0000-000000000301",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "tipoDocumento":"HISTORICO_ESCOLAR",
                          "nomeArquivo":"historico.pdf",
                          "urlArquivo":"s3://bucket/historico.pdf",
                          "numeroDocumento":"historico.pdf",
                          "caminhoArquivo":"s3://bucket/historico.pdf",
                          "dataUpload":"2026-07-12T10:00:00",
                          "observacao":"Documento escolar"
                        }
                        """));

        client.get().uri("/api/documentos-alunos/{id}", documentoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-doc-aluno-id-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(documentoId)
                .jsonPath("$.tipoDocumento").isEqualTo("HISTORICO_ESCOLAR");

        MONOLITH.takeRequest();
        var enrollmentRequest = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(enrollmentRequest.getPath()).isEqualTo("/internal/v1/documentos-alunos/" + documentoId);
        assertThat(enrollmentRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-doc-aluno-id-2");
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
