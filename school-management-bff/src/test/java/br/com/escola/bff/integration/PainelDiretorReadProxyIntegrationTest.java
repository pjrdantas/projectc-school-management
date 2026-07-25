package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
class PainelDiretorReadProxyIntegrationTest {

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
    void deveConsumirPainelQueryServiceNaLeituraOficialDoPainelDiretor() throws InterruptedException {
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
                .setHeader(TrustedHeaders.CORRELATION_ID, "corr-dashboard-dir-1")
                .setBody("""
                        {
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "totalMatriculas":10,
                          "matriculasPendentes":2,
                          "matriculasConcluidas":3,
                          "matriculasEfetivadas":4,
                          "matriculasAptasRematricula":5,
                          "alunosAtivos":6,
                          "alunosInativos":1,
                          "turmasAtivas":7,
                          "turmasLotadas":2,
                          "professoresAlocados":3,
                          "aulasRealizadas":4,
                          "avaliacoesRegistradas":5,
                          "avaliacoesComNotasPendentes":1,
                          "boletinsFechados":6,
                          "historicosInternosGerados":7,
                          "transferencias":8,
                          "solicitacoesExclusaoPendentes":1,
                          "matriculasComDocumentosPendentes":2,
                          "matriculasPorStatus":[{"status":"EM_ANDAMENTO","total":2}],
                          "turmasComVagas":[]
                        }
                        """));

        client.get().uri("/api/dashboard/diretor")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-dir-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-dashboard-dir-1")
                .expectBody()
                .jsonPath("$.escolaNome").isEqualTo("Escola padrao")
                .jsonPath("$.matriculasPendentes").isEqualTo(2)
                .jsonPath("$.avaliacoesRegistradas").isEqualTo(5);

        var authRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var dashboardRequest = DASHBOARD_QUERY.takeRequest();
        assertThat(dashboardRequest.getPath()).isEqualTo("/internal/v1/dashboard/diretor");
        assertThat(dashboardRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(dashboardRequest.getHeader("X-Internal-Token")).isEqualTo("dashboard-query-internal-token");
        assertThat(dashboardRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-dashboard-dir-1");
        assertThat(dashboardRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(dashboardRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        var monolithRequest = MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS);
        if (monolithRequest != null) {
            assertThat(monolithRequest.getPath()).isNotEqualTo("/api/dashboard/diretor");
        }
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoPainelQueryFalhar() throws InterruptedException {
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

        client.get().uri("/api/dashboard/diretor")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-dir-2")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        IDENTITY_ACCESS.takeRequest();
        var dashboardRequest = DASHBOARD_QUERY.takeRequest();
        assertThat(dashboardRequest.getPath()).isEqualTo("/internal/v1/dashboard/diretor");

        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoIdentityAccessFalharNaResolucaoDeContexto() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri("/api/dashboard/diretor")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-dir-3")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        var authRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(DASHBOARD_QUERY.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();

        assertThat(MONOLITH.getRequestCount()).isZero();
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
}

