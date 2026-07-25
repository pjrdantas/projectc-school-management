package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;
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
class PainelFrontendReadProxyIntegrationTest {

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
    void deveConsumirPainelQueryServiceNaLeituraOficialDePainelFrontend() throws InterruptedException {
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
                .setHeader(TrustedHeaders.CORRELATION_ID, "corr-dashboard-frontend-1")
                .setBody("""
                        {
                          "publicoCodigo":"DIRETOR",
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "professorId":null,
                          "resumo":{"escolaNome":"Escola padrao","totalMatriculas":120},
                          "alertas":[{"publicoCodigo":"DIRETOR","professorId":null,"codigo":"TURMAS_LOTADAS","severidade":"CRITICO","titulo":"Turmas lotadas","mensagem":"Existem turmas sem vagas disponíveis.","valor":2,"limite":0}],
                          "dashboards":[{"id":"00000000-0000-0000-0000-000000000201","codigo":"DASH_DIRETOR","nome":"Diretor","descricao":"Painel diretor","ativo":true,"widgets":[{"id":"00000000-0000-0000-0000-000000000301","codigo":"WIDGET_ALERTA","titulo":"Alertas","descricao":"Alertas críticos","tipoWidget":"CARD","ordem":1,"queryReferencia":"dashboard.alertas","ativo":true}]}],
                          "configuracoesUsuario":[],
                          "historico":[]
                        }
                        """));

        UUID usuarioId = UUID.fromString("00000000-0000-0000-0000-000000000101");

        client.get().uri(uriBuilder -> uriBuilder.path("/api/dashboard/frontend")
                        .queryParam("publicoCodigo", "diretor")
                        .queryParam("usuarioId", usuarioId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-frontend-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-dashboard-frontend-1")
                .expectBody()
                .jsonPath("$.publicoCodigo").isEqualTo("DIRETOR")
                .jsonPath("$.dashboards[0].codigo").isEqualTo("DASH_DIRETOR");

        var authRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");

        var dashboardRequest = DASHBOARD_QUERY.takeRequest();
        assertThat(dashboardRequest.getPath()).isEqualTo("/internal/v1/dashboard/frontend?publicoCodigo=diretor&usuarioId=" + usuarioId);
        assertThat(dashboardRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(dashboardRequest.getHeader("X-Internal-Token")).isEqualTo("dashboard-query-internal-token");
        var monolithRequest = MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS);
        if (monolithRequest != null) {
            assertThat(monolithRequest.getPath()).isNotEqualTo("/api/dashboard/frontend?publicoCodigo=diretor&usuarioId=" + usuarioId);
        }
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoPainelQueryServiceEstiverIndisponivel() throws InterruptedException {
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

        client.get().uri(uriBuilder -> uriBuilder.path("/api/dashboard/frontend")
                        .queryParam("publicoCodigo", "diretor")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-frontend-2")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoIdentityAccessFalhar() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/dashboard/frontend")
                        .queryParam("publicoCodigo", "diretor")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-dashboard-frontend-3")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        assertThat(MONOLITH.getRequestCount()).isZero();
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

