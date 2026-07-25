package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
class DisciplinaWriteCutoverIntegrationTest {

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
        registry.add("features.catalog-write-cutover.routes.disciplinas", () -> true);
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
    void deveRoteaCriacaoDeDisciplinaParaCatalogoQuandoPayloadForCompativel() throws Exception {
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
                  "id":"00000000-0000-0000-0000-000000000101",
                  "nome":"Matematica",
                  "cargaHoraria":80,
                  "ativo":true,
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "createdAt":"2026-06-23T10:00:00"
                }
                """, 201));

        client.post().uri("/api/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-disc-write")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"Matematica",
                          "cargaHoraria":80
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.nome").isEqualTo("Matematica")
                .jsonPath("$.status").isEqualTo("ATIVA")
                .jsonPath("$.escolaNome").isEqualTo("Escola padrao");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/disciplinas");
        assertThat(catalogRequest.getHeader("Idempotency-Key")).isNotBlank();
        assertThat(catalogRequest.getBody().readUtf8()).contains("\"nome\":\"Matematica\"").doesNotContain("status");
    }

    @Test
    void deveRotearDisciplinaInativaParaCatalogoMantendoContratoExterno() throws Exception {
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
                  "id":"00000000-0000-0000-0000-000000000102",
                  "nome":"Historia",
                  "cargaHoraria":60,
                  "ativo":false,
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "createdAt":"2026-06-23T10:05:00"
                }
                """, 201));

        client.post().uri("/api/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-disc-inativa")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"Historia",
                          "cargaHoraria":60,
                          "status":"INATIVA"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("INATIVA")
                .jsonPath("$.escolaNome").isEqualTo("Escola padrao");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/disciplinas");
        assertThat(catalogRequest.getBody().readUtf8()).contains("\"ativo\":false");
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

        client.post().uri("/api/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-disc-fail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"Matematica",
                          "cargaHoraria":80,
                          "status":"ATIVA"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/disciplinas");
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
            Path path = Files.createTempFile("catalog-disciplina-write-cutover-report", ".json");
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
