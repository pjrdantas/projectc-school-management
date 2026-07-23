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
class EscolaOrigemReadProxyIntegrationTest {

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
    void deveConsumirDocumentoMatriculaServiceNaBuscaOficialDeEscolaOrigemPorId() throws InterruptedException {
        String escolaOrigemId = "00000000-0000-0000-0000-000000000081";

        IDENTITY_ACCESS.enqueue(new MockResponse()
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
                          "id":"00000000-0000-0000-0000-000000000081",
                          "nomeEscola":"Escola Origem Oficial",
                          "codigoInep":"12345678",
                          "cidade":"Recife",
                          "uf":"PE"
                        }
                        """));

        client.get().uri("/api/escolas-origem/{escolaOrigemId}", escolaOrigemId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-escola-origem-2")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-escola-origem-2")
                .expectBody()
                .jsonPath("$.id").isEqualTo(escolaOrigemId)
                .jsonPath("$.nomeEscola").isEqualTo("Escola Origem Oficial");

        IDENTITY_ACCESS.takeRequest();
        var enrollmentRequest = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(enrollmentRequest.getPath()).isEqualTo("/internal/v1/escolas-origem/" + escolaOrigemId);
        assertThat(enrollmentRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(enrollmentRequest.getHeader("X-Internal-Token")).isEqualTo("enrollment-document-internal-token");
        assertThat(enrollmentRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-escola-origem-2");
        assertThat(enrollmentRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(enrollmentRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveConsumirDocumentoMatriculaServiceNaListagemOficialDeEscolasOrigem() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
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
                          "id":"00000000-0000-0000-0000-000000000081",
                          "nomeEscola":"Escola Origem Oficial",
                          "codigoInep":"12345678",
                          "cidade":"Recife",
                          "uf":"PE"
                        }]
                        """));

        client.get().uri("/api/escolas-origem")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-escola-origem-list-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeEscola").isEqualTo("Escola Origem Oficial");

        IDENTITY_ACCESS.takeRequest();
        var enrollmentRequest = ENROLLMENT_DOCUMENT.takeRequest();
        assertThat(enrollmentRequest.getPath()).isEqualTo("/internal/v1/escolas-origem");
        assertThat(enrollmentRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-escola-origem-list-2");
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

