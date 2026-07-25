package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import br.com.escola.bff.application.context.TrustedHeaders;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class DocumentoWriteProxyIntegrationTest {

    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer ENROLLMENT_DOCUMENT = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.enrollment-document-service.base-url", () -> ENROLLMENT_DOCUMENT.url("/").toString());
        registry.add("clients.enrollment-document-service.internal-token", () -> "enrollment-document-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        IDENTITY_ACCESS.shutdown();
        ENROLLMENT_DOCUMENT.shutdown();
    }

    @Test
    void deveOficializarCriacaoDeMetadadoDeDocumentoPorEntidade() throws InterruptedException {
        enfileirarContextoAutenticado();
        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"id\":\"00000000-0000-0000-0000-000000000401\"}"));

        String requestBody = """
                {"entidadeTipo":"MATRICULA","entidadeId":"00000000-0000-0000-0000-000000000021","tipoDocumento":"COMPROVANTE","caminhoArquivo":"comprovante.pdf"}
                """;

        client.post().uri("/api/documentos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-documento-create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("00000000-0000-0000-0000-000000000401");

        var request = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/documentos");
        assertInternalHeaders(request, "corr-documento-create");
        assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveOficializarUploadDeDocumentoPorEntidade() throws InterruptedException {
        enfileirarContextoAutenticado();
        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"id\":\"00000000-0000-0000-0000-000000000402\"}"));

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("entidadeTipo", "MATRICULA");
        body.part("entidadeId", "00000000-0000-0000-0000-000000000021");
        body.part("tipoDocumento", "COMPROVANTE");
        body.part("arquivo", "conteudo comprovante".getBytes(StandardCharsets.UTF_8))
                .filename("comprovante.pdf")
                .contentType(MediaType.APPLICATION_PDF);

        client.post().uri("/api/documentos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-documento-upload")
                .body(BodyInserters.fromMultipartData(body.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("00000000-0000-0000-0000-000000000402");

        var request = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/documentos/upload");
        assertInternalHeaders(request, "corr-documento-upload");
        String multipart = request.getBody().readUtf8();
        assertThat(multipart).contains("name=\"entidadeTipo\"");
        assertThat(multipart).contains("MATRICULA");
        assertThat(multipart).contains("name=\"entidadeId\"");
        assertThat(multipart).contains("00000000-0000-0000-0000-000000000021");
        assertThat(multipart).contains("filename=\"comprovante.pdf\"");
        assertThat(multipart).contains("conteudo comprovante");
    }

    @SuppressWarnings("resource")
	@Test
    void deveOficializarDownloadDeDocumentoPorEntidade() throws InterruptedException {
        String documentoId = "00000000-0000-0000-0000-000000000401";
        byte[] conteudo = "conteudo administrativo".getBytes(StandardCharsets.UTF_8);
        enfileirarContextoAutenticado();
        ENROLLMENT_DOCUMENT.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprovante.pdf\"")
                .setBody(new okio.Buffer().write(conteudo)));

        client.get().uri("/api/documentos/{documentoId}/conteudo", documentoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-documento-download")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprovante.pdf\"")
                .expectBody()
                .consumeWith(response -> assertThat(response.getResponseBody()).isEqualTo(conteudo));

        var request = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/documentos/" + documentoId + "/conteudo");
        assertInternalHeaders(request, "corr-documento-download");
    }

    @Test
    void deveOficializarExclusaoDeDocumentoPorEntidade() throws InterruptedException {
        String documentoId = "00000000-0000-0000-0000-000000000401";
        enfileirarContextoAutenticado();
        ENROLLMENT_DOCUMENT.enqueue(new MockResponse().setResponseCode(204));

        client.delete().uri("/api/documentos/{documentoId}", documentoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-documento-delete")
                .exchange()
                .expectStatus().isNoContent();

        var request = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/documentos/" + documentoId);
        assertInternalHeaders(request, "corr-documento-delete");
    }

    private static void enfileirarContextoAutenticado() {
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

    private static void assertInternalHeaders(okhttp3.mockwebserver.RecordedRequest request, String correlationId) {
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(request.getHeader("X-Internal-Token")).isEqualTo("enrollment-document-internal-token");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo(correlationId);
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
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
