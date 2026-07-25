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
class CatalogReadCutoverIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer CATALOG = startServer();
    private static final Path REPORT_PATH = createReportFile();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.catalog-service.base-url", () -> CATALOG.url("/").toString());
        registry.add("clients.catalog-service.internal-token", () -> "internal-token");
        registry.add("features.catalog-read-cutover.enabled", () -> true);
        registry.add("features.catalog-read-cutover.report-path", () -> REPORT_PATH.toString());
        registry.add("features.catalog-read-cutover.routes.disciplinas", () -> true);
        registry.add("features.catalog-read-cutover.routes.serie-by-id", () -> true);
        registry.add("features.catalog-read-cutover.routes.series", () -> true);
        registry.add("features.catalog-read-cutover.routes.turnos", () -> true);
        registry.add("features.catalog-read-cutover.routes.turmas", () -> true);
        registry.add("features.catalog-read-cutover.routes.turma-by-id", () -> true);
        registry.add("features.catalog-read-cutover.routes.turma-disciplinas", () -> true);
        registry.add("features.catalog-read-cutover.routes.catalogos-niveis-ensino", () -> true);
        registry.add("features.catalog-read-cutover.routes.catalogos-turnos", () -> true);
        registry.add("features.catalog-read-cutover.routes.turno-by-id", () -> true);
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        CATALOG.shutdown();
        Files.deleteIfExists(REPORT_PATH);
    }

    @Test
    void deveRoteaDisciplinasParaCatalogoQuandoRelatorioEstiverReconciliado() throws Exception {
        MONOLITH.enqueue(json("""
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
                  "nome":"Matematica catalog",
                  "cargaHoraria":80,
                  "status":"ATIVA",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "createdAt":"2026-06-22T12:00:00"
                }]
                """));

        client.get().uri("/api/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-cutover")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nome").isEqualTo("Matematica catalog");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(contextRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");

        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/disciplinas");
        assertThat(catalogRequest.getHeader("X-Internal-Token")).isEqualTo("internal-token");
        assertThat(catalogRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000201");
        assertThat(catalogRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoCatalogoFalhar() throws Exception {
        int monolithRequestCount = MONOLITH.getRequestCount();
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri("/api/turnos/00000000-0000-0000-0000-000000000051")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-fallback")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/turnos/00000000-0000-0000-0000-000000000051");
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithRequestCount + 1);
    }

    @Test
    void deveRoteaSeriesParaCatalogoQuandoRelatorioEstiverReconciliado() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                [{
                  "id":"00000000-0000-0000-0000-000000000061",
                  "nome":"1 Ano",
                  "nivelEnsinoId":"00000000-0000-0000-0000-000000000011",
                  "nivelEnsinoNome":"Fundamental I",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "createdAt":"2026-06-22T12:00:00"
                }]
                """));

        client.get().uri("/api/series")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-series")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nome").isEqualTo("1 Ano");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/series");
        assertThat(catalogRequest.getHeader("X-Internal-Token")).isEqualTo("internal-token");
    }

    @Test
    void deveRoteaSeriePorIdParaCatalogoQuandoRelatorioEstiverReconciliado() throws Exception {
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
                  "id":"00000000-0000-0000-0000-000000000061",
                  "nome":"1 Ano",
                  "nivelEnsinoId":"00000000-0000-0000-0000-000000000011",
                  "nivelEnsinoNome":"Fundamental I",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "createdAt":"2026-06-22T12:00:00"
                }
                """));

        client.get().uri("/api/series/00000000-0000-0000-0000-000000000061")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-serie-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nome").isEqualTo("1 Ano");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/series/00000000-0000-0000-0000-000000000061");
    }

    @Test
    void deveRoteaTurmasParaCatalogoQuandoRelatorioEstiverReconciliado() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                [{
                  "id":"00000000-0000-0000-0000-000000000071",
                  "nome":"Turma A",
                  "serieId":"00000000-0000-0000-0000-000000000061",
                  "serieNome":"1 Ano",
                  "turnoId":"00000000-0000-0000-0000-000000000051",
                  "turnoNome":"Manha",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "createdAt":"2026-06-22T12:00:00"
                }]
                """));

        client.get().uri("/api/turmas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-turmas")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nome").isEqualTo("Turma A");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/turmas");
    }

    @Test
    void deveRetornarIndisponibilidadeEmTurmaPorIdQuandoCatalogoFalhar() throws Exception {
        int monolithRequestCount = MONOLITH.getRequestCount();
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri("/api/turmas/00000000-0000-0000-0000-000000000071")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-turma-id-fallback")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/turmas/00000000-0000-0000-0000-000000000071");
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithRequestCount + 1);
    }

    @Test
    void deveRoteaDisciplinasDaTurmaParaCatalogoQuandoRelatorioEstiverReconciliado() throws Exception {
        MONOLITH.enqueue(json("""
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
                  "createdAt":"2026-06-22T12:00:00"
                }]
                """));

        client.get().uri("/api/turmas/00000000-0000-0000-0000-000000000071/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-turma-disciplinas")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nome").isEqualTo("Matematica");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/turmas/00000000-0000-0000-0000-000000000071/disciplinas");
    }

    @Test
    void deveRoteaTurnosParaCatalogoQuandoRelatorioEstiverReconciliado() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                [{
                  "id":"00000000-0000-0000-0000-000000000051",
                  "codigo":"MANHA",
                  "descricao":"Manha"
                }]
                """));

        client.get().uri("/api/turnos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-turnos")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].codigo").isEqualTo("MANHA");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/turnos");
    }

    @Test
    void deveRoteaCatalogoTurnosParaCatalogoQuandoRelatorioEstiverReconciliado() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(json("""
                [{
                  "id":"00000000-0000-0000-0000-000000000051",
                  "codigo":"MANHA",
                  "descricao":"Manha"
                }]
                """));

        client.get().uri("/api/academico/catalogos/turnos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-catalogo-turnos")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].codigo").isEqualTo("MANHA");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/turnos");
    }

    @Test
    void deveRetornarIndisponibilidadeEmCatalogoNiveisEnsinoQuandoCatalogoFalhar() throws Exception {
        int monolithRequestCount = MONOLITH.getRequestCount();
        MONOLITH.enqueue(json("""
                {
                  "usuarioId":"00000000-0000-0000-0000-000000000201",
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola padrao",
                  "username":"admin"
                }
                """));
        CATALOG.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri("/api/academico/catalogos/niveis-ensino")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-catalogo-niveis-fallback")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        var contextRequest = MONOLITH.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var catalogRequest = CATALOG.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/catalogos/niveis-ensino");
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithRequestCount + 1);
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
            Path path = Files.createTempFile("catalog-cutover-report", ".json");
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
