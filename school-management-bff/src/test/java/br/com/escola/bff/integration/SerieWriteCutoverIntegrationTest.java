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
class SerieWriteCutoverIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer CATALOG = startServer();
    private static final Path REPORT_PATH = createReportFile();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.catalog-service.base-url", () -> CATALOG.url("/").toString());
        registry.add("clients.catalog-service.internal-token", () -> "internal-token");
        registry.add("features.catalog-write-cutover.enabled", () -> true);
        registry.add("features.catalog-write-cutover.routes.series", () -> true);
        registry.add("features.catalog-read-cutover.report-path", () -> REPORT_PATH.toString());
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        CATALOG.shutdown();
        Files.deleteIfExists(REPORT_PATH);
    }

    @Test
    void deveRoteaCriacaoDeSerieParaCatalogoQuandoContratoEstiverCompativel() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                [
                  {
                    "id":"00000000-0000-0000-0000-000000000011",
                    "codigo":"ENSINO_FUNDAMENTAL",
                    "descricao":"Fundamental"
                  }
                ]
                """));
        CATALOG.enqueue(json("""
                {
                  "id":"00000000-0000-0000-0000-000000000061",
                  "nome":"1 ano",
                  "ordem":1,
                  "nivelEnsinoId":"00000000-0000-0000-0000-000000000011",
                  "nivelEnsinoCodigo":"ENSINO_FUNDAMENTAL",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "createdAt":"2026-06-23T11:10:00"
                }
                """, 201));

        client.post().uri("/api/series")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-serie-write")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"1 ano",
                          "ordem":1,
                          "nivelEnsino":"ENSINO_FUNDAMENTAL"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.nome").isEqualTo("1 ano")
                .jsonPath("$.nivelEnsino").isEqualTo("ENSINO_FUNDAMENTAL")
                .jsonPath("$.escolaNome").isEqualTo("Escola padrao");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var niveisRequest = CATALOG.takeRequest();
        assertThat(niveisRequest.getPath()).isEqualTo("/internal/v1/catalogos/niveis-ensino");
        var serieRequest = CATALOG.takeRequest();
        assertThat(serieRequest.getPath()).isEqualTo("/internal/v1/series");
        assertThat(serieRequest.getBody().readUtf8())
                .contains("\"nivelEnsinoId\":\"00000000-0000-0000-0000-000000000011\"")
                .doesNotContain("\"escolaId\"");
    }

    @Test
    void deveRejeitarQuandoNivelEnsinoNaoForResolvidoNoCatalogoOficial() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                [
                  {
                    "id":"00000000-0000-0000-0000-000000000011",
                    "codigo":"ENSINO_FUNDAMENTAL",
                    "descricao":"Fundamental"
                  }
                ]
                """));

        client.post().uri("/api/series")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-serie-invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"1 ano",
                          "ordem":1,
                          "nivelEnsino":"ENSINO_MEDIO"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var niveisRequest = CATALOG.takeRequest();
        assertThat(niveisRequest.getPath()).isEqualTo("/internal/v1/catalogos/niveis-ensino");
        assertThat(MONOLITH.getRequestCount()).isEqualTo(1);
        assertThat(CATALOG.getRequestCount()).isEqualTo(1);
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalharNaEscrita() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                [
                  {
                    "id":"00000000-0000-0000-0000-000000000011",
                    "codigo":"ENSINO_FUNDAMENTAL",
                    "descricao":"Fundamental"
                  }
                ]
                """));
        CATALOG.enqueue(new MockResponse().setResponseCode(503));

        client.post().uri("/api/series")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-serie-fail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"1 ano",
                          "ordem":1,
                          "nivelEnsino":"ENSINO_FUNDAMENTAL"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var niveisRequest = CATALOG.takeRequest();
        assertThat(niveisRequest.getPath()).isEqualTo("/internal/v1/catalogos/niveis-ensino");
        var serieRequest = CATALOG.takeRequest();
        assertThat(serieRequest.getPath()).isEqualTo("/internal/v1/series");
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
            Path path = Files.createTempFile("catalog-serie-write-cutover-report", ".json");
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
        return json(body, 200);
    }

    private static MockResponse json(String body, int status) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }
}
