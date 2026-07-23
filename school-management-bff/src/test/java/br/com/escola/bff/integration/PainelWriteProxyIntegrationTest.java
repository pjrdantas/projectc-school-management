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
class PainelWriteProxyIntegrationTest {

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
    void deveEncaminharWritesDoPainelSomenteAoDashboardQueryService() throws Exception {
        UUID publicoId = UUID.randomUUID();
        UUID painelId = UUID.randomUUID();
        UUID widgetId = UUID.randomUUID();
        UUID usuarioId = UUID.fromString("00000000-0000-0000-0000-000000000101");

        responderContexto();
        responderDashboard(201, "{\"id\":\"" + publicoId + "\"}");
        requisicaoPost("/api/dashboard/configuracoes/publicos", "{\"codigo\":\"diretor\",\"descricao\":\"Diretoria\"}")
                .expectStatus().isCreated();
        var publico = DASHBOARD_QUERY.takeRequest();
        assertThat(publico.getPath()).isEqualTo("/internal/v1/dashboard/configuracoes/publicos");

        responderContexto();
        responderDashboard(201, "{\"id\":\"" + painelId + "\"}");
        requisicaoPost("/api/dashboard/configuracoes/dashboards", "{\"publicoDashboardId\":\"" + publicoId
                + "\",\"codigo\":\"geral\",\"nome\":\"Geral\",\"ativo\":true}")
                .expectStatus().isCreated();
        var painel = DASHBOARD_QUERY.takeRequest();
        assertThat(painel.getPath()).isEqualTo("/internal/v1/dashboard/configuracoes/dashboards");
        assertThat(painel.getBody().readUtf8()).contains("\"publicoId\":\"" + publicoId + "\"")
                .doesNotContain("publicoDashboardId");

        responderContexto();
        responderDashboard(201, "{\"id\":\"" + widgetId + "\"}");
        requisicaoPost("/api/dashboard/configuracoes/widgets", "{\"dashboardId\":\"" + painelId
                + "\",\"codigo\":\"matriculas\",\"titulo\":\"Matriculas\",\"tipoWidget\":\"INDICADOR\",\"ordem\":0}")
                .expectStatus().isCreated();
        var widget = DASHBOARD_QUERY.takeRequest();
        assertThat(widget.getPath()).isEqualTo("/internal/v1/dashboard/configuracoes/widgets");
        assertThat(widget.getBody().readUtf8()).contains("\"painelId\":\"" + painelId + "\"")
                .doesNotContain("dashboardId");

        responderContexto();
        responderDashboard(200, "{}");
        client.put().uri("/api/dashboard/usuarios/{usuarioId}/widgets/{widgetId}/configuracao", usuarioId, widgetId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token").header(TrustedHeaders.CORRELATION_ID, "corr-painel-write")
                .contentType(MediaType.APPLICATION_JSON).bodyValue("{\"visivel\":true}").exchange().expectStatus().isOk();
        assertThat(DASHBOARD_QUERY.takeRequest().getPath()).isEqualTo(
                "/internal/v1/dashboard/usuarios/" + usuarioId + "/widgets/" + widgetId + "/configuracao");

        responderContexto();
        responderDashboard(200, "{}");
        client.put().uri("/api/dashboard/snapshots").header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-painel-write").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"publicoDashboardId\":\"" + publicoId + "\",\"codigoIndicador\":\"TOTAL_MATRICULAS\",\"descricao\":\"Total\",\"referenciaData\":\"2026-07-23\"}")
                .exchange().expectStatus().isOk();
        var snapshot = DASHBOARD_QUERY.takeRequest();
        assertThat(snapshot.getPath()).isEqualTo("/internal/v1/dashboard/snapshots/locais");
        assertThat(snapshot.getBody().readUtf8()).contains("\"publicoId\":\"" + publicoId + "\"");

        responderContexto();
        responderDashboard(200, "[]");
        requisicaoPost("/api/dashboard/snapshots/geracoes/ACADEMICO?referenciaData=2026-07-23", "")
                .expectStatus().isOk();
        assertThat(DASHBOARD_QUERY.takeRequest().getPath()).isEqualTo(
                "/internal/v1/dashboard/snapshots/geracoes/ACADEMICO?referenciaData=2026-07-23");

        UUID professorId = UUID.randomUUID();
        responderContexto();
        responderDashboard(200, "[]");
        requisicaoPost("/api/dashboard/snapshots/geracoes/professores/" + professorId + "?referenciaData=2026-07-23", "")
                .expectStatus().isOk();
        assertThat(DASHBOARD_QUERY.takeRequest().getPath()).isEqualTo(
                "/internal/v1/dashboard/snapshots/geracoes/professores/" + professorId + "?referenciaData=2026-07-23");

        assertThat(MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
    }

    private WebTestClient.ResponseSpec requisicaoPost(String path, String body) {
        return client.post().uri(path).header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-painel-write")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(body).exchange();
    }

    private static void responderContexto() {
        IDENTITY_ACCESS.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody("""
                {"usuarioId":"00000000-0000-0000-0000-000000000101",
                 "escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                """));
    }

    private static void responderDashboard(int status, String body) {
        DASHBOARD_QUERY.enqueue(new MockResponse().setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(body));
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
