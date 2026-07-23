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
class PainelCompatibilidadeReadProxyIntegrationTest {

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
    void deveServirWidgetsEPreferenciasPeloDashboardQueryService() throws Exception {
        UUID painelId = UUID.randomUUID();
        UUID usuarioId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        responderContexto();
        DASHBOARD_QUERY.enqueue(json("[{\"id\":\"" + UUID.randomUUID() + "\",\"codigo\":\"MATRICULAS\"}]"));
        client.get().uri("/api/dashboard/configuracoes/dashboards/{painelId}/widgets", painelId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-painel-compatibilidade")
                .exchange().expectStatus().isOk().expectBody().jsonPath("$[0].codigo").isEqualTo("MATRICULAS");
        assertThat(DASHBOARD_QUERY.takeRequest().getPath()).isEqualTo(
                "/internal/v1/dashboard/configuracoes/dashboards/" + painelId + "/widgets");

        responderContexto();
        DASHBOARD_QUERY.enqueue(json("[{\"usuarioId\":\"" + usuarioId + "\",\"painelId\":\"" + painelId + "\"}]"));
        client.get().uri(uri -> uri.path("/api/dashboard/usuarios/{usuarioId}/configuracoes")
                        .queryParam("dashboardId", painelId).build(usuarioId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-painel-compatibilidade")
                .exchange().expectStatus().isOk().expectBody().jsonPath("$[0].painelId").isEqualTo(painelId.toString());
        assertThat(DASHBOARD_QUERY.takeRequest().getPath()).isEqualTo(
                "/internal/v1/dashboard/usuarios/" + usuarioId + "/configuracoes?painelId=" + painelId);
        assertThat(MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void deveServirSnapshotsPorIdentificadorDoPublicoPeloReadModelLocal() throws Exception {
        UUID publicoId = UUID.randomUUID();
        responderContexto();
        DASHBOARD_QUERY.enqueue(json("[{\"publicoPainelId\":\"" + publicoId
                + "\",\"codigoIndicador\":\"TOTAL_MATRICULAS\",\"valorNumeric\":12}]"));

        client.get().uri(uri -> uri.path("/api/dashboard/snapshots")
                        .queryParam("publicoDashboardId", publicoId)
                        .queryParam("referenciaData", "2026-07-23").build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-painel-snapshot")
                .exchange().expectStatus().isOk().expectBody().jsonPath("$[0].valorNumeric").isEqualTo(12);

        assertThat(DASHBOARD_QUERY.takeRequest().getPath()).isEqualTo(
                "/internal/v1/dashboard/snapshots/locais?publicoId=" + publicoId + "&referenciaData=2026-07-23");
        assertThat(MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
    }

    private static MockResponse json(String body) {
        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(body);
    }

    private static void responderContexto() {
        IDENTITY_ACCESS.enqueue(json("""
                {"usuarioId":"00000000-0000-0000-0000-000000000101",
                 "escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                """));
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
