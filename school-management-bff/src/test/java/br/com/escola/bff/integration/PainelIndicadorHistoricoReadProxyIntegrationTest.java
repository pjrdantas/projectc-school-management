package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
class PainelIndicadorHistoricoReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer DASHBOARD_QUERY = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.dashboard-query-service.base-url", () -> DASHBOARD_QUERY.url("/").toString());
        registry.add("clients.dashboard-query-service.internal-token", () -> "dashboard-query-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
        DASHBOARD_QUERY.shutdown();
    }

    @Test
    void deveConsumirPainelQueryServiceNaLeituraOficialDeHistorico() throws InterruptedException {
        UUID professorId = UUID.randomUUID();
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        DASHBOARD_QUERY.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setHeader(TrustedHeaders.CORRELATION_ID, "corr-dashboard-historico-1")
                .setBody("""
                        [{
                          "publicoCodigo":"DASH-SNAP-PROFESSOR",
                          "codigoIndicador":"PROFESSOR_INDICADOR",
                          "descricao":"Indicador do professor",
                          "valorAtual":6,
                          "valorAnterior":4,
                          "variacaoPercentual":50,
                          "pontos":[
                            {"referenciaData":"2026-07-15","valorNumeric":4,"valorTexto":"4"},
                            {"referenciaData":"2026-07-16","valorNumeric":6,"valorTexto":"6"}
                          ]
                        }]
                        """));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/dashboard/snapshots/historico/publicos/{publicoCodigo}")
                        .queryParam("codigoIndicador", "aulas_realizadas")
                        .queryParam("dataInicio", "2026-07-15")
                        .queryParam("dataFim", "2026-07-16")
                        .queryParam("professorId", professorId)
                        .build("dash-snap-professor"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-historico-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-dashboard-historico-1")
                .expectBody()
                .jsonPath("$[0].codigoIndicador").isEqualTo("PROFESSOR_INDICADOR")
                .jsonPath("$[0].pontos[1].referenciaData").isEqualTo("2026-07-16");

        var authRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var dashboardRequest = DASHBOARD_QUERY.takeRequest();
        assertThat(dashboardRequest.getPath()).isEqualTo(
                "/internal/v1/dashboard/snapshots/historico/publicos/dash-snap-professor?codigoIndicador=aulas_realizadas&dataInicio=2026-07-15&dataFim=2026-07-16&professorId="
                        + professorId);
        assertThat(dashboardRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(dashboardRequest.getHeader("X-Internal-Token")).isEqualTo("dashboard-query-internal-token");
        var monolithRequest = MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS);
        if (monolithRequest != null) {
            assertThat(monolithRequest.getPath()).isNotEqualTo(
                    "/api/dashboard/snapshots/historico/publicos/dash-snap-professor?codigoIndicador=aulas_realizadas&dataInicio=2026-07-15&dataFim=2026-07-16&professorId="
                            + professorId);
        }
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoPainelQueryServiceEstiverIndisponivel() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        DASHBOARD_QUERY.enqueue(new MockResponse().setResponseCode(503));
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "publicoCodigo":"DASH-SNAP-DIRETOR",
                          "codigoIndicador":"TOTAL_MATRICULAS",
                          "descricao":"Total de matriculas",
                          "valorAtual":118,
                          "valorAnterior":117,
                          "variacaoPercentual":0.85,
                          "pontos":[]
                        }]
                        """));

        client.get().uri("/api/dashboard/snapshots/historico/publicos/dash-snap-diretor")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-historico-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].valorAtual").isEqualTo(118);

        var monolithRequest = MONOLITH.takeRequest();
        assertThat(monolithRequest.getPath()).isEqualTo("/api/dashboard/snapshots/historico/publicos/dash-snap-diretor");
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoIdentityAccessFalhar() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse().setResponseCode(503));
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "publicoCodigo":"DASH-SNAP-DIRETOR",
                          "codigoIndicador":"TOTAL_MATRICULAS",
                          "descricao":"Total de matriculas",
                          "valorAtual":117,
                          "valorAnterior":116,
                          "variacaoPercentual":0.86,
                          "pontos":[]
                        }]
                        """));

        client.get().uri("/api/dashboard/snapshots/historico/publicos/dash-snap-diretor")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-historico-3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].valorAtual").isEqualTo(117);

        var monolithRequest = MONOLITH.takeRequest();
        assertThat(monolithRequest.getPath()).isEqualTo("/api/dashboard/snapshots/historico/publicos/dash-snap-diretor");
    }

    private static MockWebServer startServer() {
        try {
            MockWebServer server = new MockWebServer();
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel iniciar MockWebServer", exception);
        }
    }
}

