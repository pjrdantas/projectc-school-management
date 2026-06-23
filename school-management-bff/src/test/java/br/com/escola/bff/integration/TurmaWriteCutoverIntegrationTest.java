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
class TurmaWriteCutoverIntegrationTest {

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
        registry.add("features.catalog-write-cutover.routes.turmas", () -> true);
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
    void deveRoteaCriacaoDeTurmaParaCatalogoQuandoContratoEstiverCompativel() throws Exception {
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
                    "id":"00000000-0000-0000-0000-000000000211",
                    "codigo":"MANHA",
                    "descricao":"Manhã"
                  }
                ]
                """));
        CATALOG.enqueue(json("""
                {
                  "id":"00000000-0000-0000-0000-000000000071",
                  "codigo":"A",
                  "nome":"Turma A",
                  "capacidade":30,
                  "periodoLetivoId":"00000000-0000-0000-0000-000000000081",
                  "serieId":"00000000-0000-0000-0000-000000000061",
                  "serieNome":"1 ano",
                  "turnoId":"00000000-0000-0000-0000-000000000211",
                  "turnoCodigo":"MANHA",
                  "ativo":true,
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "createdAt":"2026-06-23T11:40:00"
                }
                """, 201));

        client.post().uri("/api/turmas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-turma-write")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "codigo":"A",
                          "nome":"Turma A",
                          "capacidade":30,
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000081",
                          "serieId":"00000000-0000-0000-0000-000000000061",
                          "turno":"MANHA",
                          "status":"ATIVA"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("A")
                .jsonPath("$.turno").isEqualTo("MANHA")
                .jsonPath("$.status").isEqualTo("ATIVA")
                .jsonPath("$.escolaNome").isEqualTo("Escola padrao");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/api/auth/contexto-atual");
        var turnosRequest = CATALOG.takeRequest();
        assertThat(turnosRequest.getPath()).isEqualTo("/internal/v1/turnos");
        var turmaRequest = CATALOG.takeRequest();
        assertThat(turmaRequest.getPath()).isEqualTo("/internal/v1/turmas");
        assertThat(turmaRequest.getBody().readUtf8())
                .contains("\"turnoId\":\"00000000-0000-0000-0000-000000000211\"")
                .doesNotContain("\"status\"")
                .doesNotContain("\"escolaId\"");
    }

    @Test
    void deveVoltarAoMonolitoQuandoTurnoNaoForResolvidoNoCatalogoNovo() throws Exception {
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
                    "id":"00000000-0000-0000-0000-000000000211",
                    "codigo":"MANHA",
                    "descricao":"Manhã"
                  }
                ]
                """));
        MONOLITH.enqueue(json("""
                {
                  "id":"00000000-0000-0000-0000-000000000072",
                  "codigo":"A",
                  "nome":"Turma A",
                  "capacidade":30,
                  "periodoLetivoId":"00000000-0000-0000-0000-000000000081",
                  "serieId":"00000000-0000-0000-0000-000000000061",
                  "serieNome":"1 ano",
                  "turno":"NOITE",
                  "status":"ATIVA",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola monolito",
                  "createdAt":"2026-06-23T11:41:00"
                }
                """, 201));

        client.post().uri("/api/turmas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-turma-monolith")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "codigo":"A",
                          "nome":"Turma A",
                          "capacidade":30,
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000081",
                          "serieId":"00000000-0000-0000-0000-000000000061",
                          "turno":"NOITE",
                          "status":"ATIVA"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.turno").isEqualTo("NOITE")
                .jsonPath("$.escolaNome").isEqualTo("Escola monolito");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/api/auth/contexto-atual");
        var turnosRequest = CATALOG.takeRequest();
        assertThat(turnosRequest.getPath()).isEqualTo("/internal/v1/turnos");
        var monolithRequest = MONOLITH.takeRequest();
        assertThat(monolithRequest.getPath()).isEqualTo("/api/turmas");
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
                    "id":"00000000-0000-0000-0000-000000000211",
                    "codigo":"MANHA",
                    "descricao":"Manhã"
                  }
                ]
                """));
        CATALOG.enqueue(new MockResponse().setResponseCode(503));

        client.post().uri("/api/turmas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-turma-fail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "codigo":"A",
                          "nome":"Turma A",
                          "capacidade":30,
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000081",
                          "serieId":"00000000-0000-0000-0000-000000000061",
                          "turno":"MANHA",
                          "status":"ATIVA"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/api/auth/contexto-atual");
        var turnosRequest = CATALOG.takeRequest();
        assertThat(turnosRequest.getPath()).isEqualTo("/internal/v1/turnos");
        var turmaRequest = CATALOG.takeRequest();
        assertThat(turmaRequest.getPath()).isEqualTo("/internal/v1/turmas");
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
            Path path = Files.createTempFile("catalog-turma-write-cutover-report", ".json");
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
