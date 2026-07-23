package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
class DisciplinaProxyIntegrationTest {

    private static final MockWebServer AUTH = startServer();
    private static final MockWebServer CATALOG = startServer();
    private static final Path REPORT_PATH = createReportFile();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> AUTH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> AUTH.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.catalog-service.base-url", () -> CATALOG.url("/").toString());
        registry.add("clients.catalog-service.internal-token", () -> "internal-token");
        registry.add("features.catalog-read-cutover.enabled", () -> true);
        registry.add("features.catalog-read-cutover.routes.disciplinas", () -> true);
        registry.add("features.catalog-read-cutover.report-path", () -> REPORT_PATH.toString());
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        AUTH.shutdown();
        CATALOG.shutdown();
        Files.deleteIfExists(REPORT_PATH);
    }

    @Test
    void deveExecutarFluxoCompletoDaRotaPiloto() throws InterruptedException {
        AUTH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                [{
                  "id":"00000000-0000-0000-0000-000000000001",
                  "nome":"Matematica",
                  "cargaHoraria":80,
                  "status":"ATIVA",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "createdAt":"2026-06-19T10:00:00"
                }]
                """));

        client.get().uri("/api/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-e2e")
                .header("X-Escola-Id", "forged")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-e2e")
                .expectBody()
                .jsonPath("$[0].nome").isEqualTo("Matematica");

        var authRequest = AUTH.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(authRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");

        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/disciplinas");
        assertThat(catalogRequest.getHeader(TrustedHeaders.CORRELATION_ID)).isEqualTo("corr-e2e");
        assertThat(catalogRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000201");
        assertThat(catalogRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveRejeitarSemBearerAntesDeChamarServicosInternos() {
        int authRequestsBefore = AUTH.getRequestCount();
        int catalogRequestsBefore = CATALOG.getRequestCount();

        client.get().uri("/api/disciplinas")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED");

        assertThat(AUTH.getRequestCount()).isEqualTo(authRequestsBefore);
        assertThat(CATALOG.getRequestCount()).isEqualTo(catalogRequestsBefore);
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

    private static Path createReportFile() {
        try {
            Path path = Files.createTempFile("catalog-disciplina-proxy-report", ".json");
            Files.writeString(path, """
                    {
                      "applied": true,
                      "reconciled": true,
                      "sourceIssues": [],
                      "targetIssues": [],
                      "tables": [
                        {
                          "missingIds": [],
                          "unexpectedIds": [],
                          "divergentIds": []
                        }
                      ]
                    }
                    """);
            return path;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }
}
