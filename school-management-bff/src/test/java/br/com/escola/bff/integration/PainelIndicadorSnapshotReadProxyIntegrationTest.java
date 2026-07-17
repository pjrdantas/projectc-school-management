package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
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
class PainelIndicadorSnapshotReadProxyIntegrationTest {

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
    void deveConsumirPainelQueryServiceNaLeituraOficialDeSnapshots() throws InterruptedException {
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
                .setHeader(TrustedHeaders.CORRELATION_ID, "corr-dashboard-snapshot-1")
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000501",
                          "publicoPainelId":"00000000-0000-0000-0000-000000000201",
                          "publicoCodigo":"DASH-SNAP-DIRETOR",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "codigoIndicador":"TOTAL_MATRICULAS",
                          "descricao":"Total de matriculas",
                          "valorNumeric":120,
                          "valorTexto":"120",
                          "referenciaData":"2026-07-16"
                        }]
                        """));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/dashboard/snapshots/publicos/{publicoCodigo}")
                        .queryParam("referenciaData", "2026-07-16")
                        .build("dash-snap-diretor"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-snapshot-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-dashboard-snapshot-1")
                .expectBody()
                .jsonPath("$[0].publicoCodigo").isEqualTo("DASH-SNAP-DIRETOR")
                .jsonPath("$[0].codigoIndicador").isEqualTo("TOTAL_MATRICULAS");

        var authRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var dashboardRequest = DASHBOARD_QUERY.takeRequest();
        assertThat(dashboardRequest.getPath())
                .isEqualTo("/internal/v1/dashboard/snapshots/publicos/dash-snap-diretor?referenciaData=2026-07-16");
        assertThat(dashboardRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(dashboardRequest.getHeader("X-Internal-Token")).isEqualTo("dashboard-query-internal-token");
        var monolithRequest = MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS);
        if (monolithRequest != null) {
            assertThat(monolithRequest.getPath())
                    .isNotEqualTo("/api/dashboard/snapshots/publicos/dash-snap-diretor?referenciaData=2026-07-16");
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
                .setHeader(TrustedHeaders.CORRELATION_ID, "corr-dashboard-snapshot-2")
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000502",
                          "publicoPainelId":"00000000-0000-0000-0000-000000000202",
                          "publicoCodigo":"DASH-SNAP-DIRETOR",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola monolito",
                          "codigoIndicador":"TOTAL_MATRICULAS",
                          "descricao":"Total de matriculas",
                          "valorNumeric":118,
                          "valorTexto":"118",
                          "referenciaData":"2026-07-16"
                        }]
                        """));

        client.get().uri("/api/dashboard/snapshots/publicos/dash-snap-diretor")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-snapshot-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].escolaNome").isEqualTo("Escola monolito");

        var monolithRequest = MONOLITH.takeRequest();
        assertThat(monolithRequest.getPath()).isEqualTo("/api/dashboard/snapshots/publicos/dash-snap-diretor");
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoIdentityAccessFalhar() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse().setResponseCode(503));
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000503",
                          "publicoPainelId":"00000000-0000-0000-0000-000000000203",
                          "publicoCodigo":"DASH-SNAP-DIRETOR",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Fallback monolito",
                          "codigoIndicador":"TOTAL_MATRICULAS",
                          "descricao":"Total de matriculas",
                          "valorNumeric":117,
                          "valorTexto":"117",
                          "referenciaData":"2026-07-16"
                        }]
                        """));

        client.get().uri("/api/dashboard/snapshots/publicos/dash-snap-diretor")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-snapshot-3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].escolaNome").isEqualTo("Fallback monolito");

        var monolithRequest = MONOLITH.takeRequest();
        assertThat(monolithRequest.getPath()).isEqualTo("/api/dashboard/snapshots/publicos/dash-snap-diretor");
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

