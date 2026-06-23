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
class TurmaDisciplinaWriteMonolithIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer CATALOG = startServer();
    private static final Path REPORT_PATH = createReportFile();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.catalog-service.base-url", () -> CATALOG.url("/").toString());
        registry.add("features.catalog-write-cutover.enabled", () -> false);
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
    void deveRoteaVinculoDeDisciplinaParaMonolitoQuandoCutoverEstiverDesabilitado() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "id":"00000000-0000-0000-0000-000000000301",
                  "turmaId":"00000000-0000-0000-0000-000000000071",
                  "disciplinaId":"00000000-0000-0000-0000-000000000081",
                  "disciplinaNome":"Matematica monolito",
                  "cargaHoraria":80,
                  "createdAt":"2026-06-23T13:11:00"
                }
                """, 201));

        client.post().uri("/api/turmas/00000000-0000-0000-0000-000000000071/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-turma-disc-monolith")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "disciplinaId":"00000000-0000-0000-0000-000000000081",
                          "cargaHoraria":80
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.disciplinaNome").isEqualTo("Matematica monolito");

        var monolithRequest = MONOLITH.takeRequest();
        assertThat(monolithRequest.getPath())
                .isEqualTo("/api/turmas/00000000-0000-0000-0000-000000000071/disciplinas");
        assertThat(CATALOG.getRequestCount()).isZero();
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
            Path path = Files.createTempFile("catalog-turma-disciplina-write-monolith-report", ".json");
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

    private static MockResponse json(String body, int status) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }
}
