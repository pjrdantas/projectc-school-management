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
class PeriodoLetivoWriteCutoverIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer CATALOG = startServer();
    private static final Path REPORT_PATH = createReportFile();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.catalog-service.base-url", () -> CATALOG.url("/").toString());
        registry.add("clients.catalog-service.internal-token", () -> "internal-token");
        registry.add("features.catalog-write-cutover.enabled", () -> true);
        registry.add("features.catalog-write-cutover.routes.periodos-letivos", () -> true);
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
    void deveRoteaCriacaoDePeriodoParaCatalogoQuandoCutoverEscritaEstiverAtivo() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                {
                  "id":"00000000-0000-0000-0000-000000000081",
                  "nome":"2026",
                  "ano":2026,
                  "dataInicio":"2026-01-10",
                  "dataFim":"2026-12-20",
                  "ativo":true,
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "createdAt":"2026-06-23T09:00:00"
                }
                """, 201));

        client.post().uri("/api/periodos-letivos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-write")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"2026",
                          "dataInicio":"2026-01-10",
                          "dataFim":"2026-12-20"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.nome").isEqualTo("2026")
                .jsonPath("$.escolaNome").isEqualTo("Escola padrao")
                .jsonPath("$.ano").isEqualTo(2026);

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/api/auth/contexto-atual");
        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/periodos-letivos");
        assertThat(catalogRequest.getHeader("X-Internal-Token")).isEqualTo("internal-token");
        assertThat(catalogRequest.getHeader("Idempotency-Key")).isNotBlank();
        assertThat(catalogRequest.getBody().readUtf8()).contains("\"ano\":2026").doesNotContain("escolaId");
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
        CATALOG.enqueue(new MockResponse().setResponseCode(503));

        client.post().uri("/api/periodos-letivos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-write-fail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"2026",
                          "dataInicio":"2026-01-10",
                          "dataFim":"2026-12-20"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/api/auth/contexto-atual");
        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/periodos-letivos");
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
            Path path = Files.createTempFile("catalog-write-cutover-report", ".json");
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
