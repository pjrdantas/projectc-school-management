package br.com.escola.dashboardqueryservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest
@AutoConfigureMockMvc
class PainelFrontendInternalControllerIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("dashboard-query.internal-api.token", () -> "dashboard-token");
        registry.add("dashboard-query.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveConsultarPainelFrontendNoContratoInterno() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "publicoCodigo":"DIRETOR",
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "professorId":null,
                          "resumo":{"escolaNome":"Escola padrao","totalMatriculas":120},
                          "alertas":[{"publicoCodigo":"DIRETOR","professorId":null,"codigo":"TURMAS_LOTADAS","severidade":"CRITICO","titulo":"Turmas lotadas","mensagem":"Existem turmas sem vagas disponíveis.","valor":2,"limite":0}],
                          "dashboards":[{"id":"00000000-0000-0000-0000-000000000201","codigo":"DASH_DIRETOR","nome":"Diretor","descricao":"Painel diretor","ativo":true,"widgets":[{"id":"00000000-0000-0000-0000-000000000301","codigo":"WIDGET_ALERTA","titulo":"Alertas","descricao":"Alertas críticos","tipoWidget":"CARD","ordem":1,"queryReferencia":"dashboard.alertas","ativo":true}]}],
                          "configuracoesUsuario":[{"id":"00000000-0000-0000-0000-000000000401","usuarioId":"00000000-0000-0000-0000-000000000101","dashboardWidgetId":"00000000-0000-0000-0000-000000000301","widgetCodigo":"WIDGET_ALERTA","widgetTitulo":"Alertas","dashboardId":"00000000-0000-0000-0000-000000000201","dashboardCodigo":"DASH_DIRETOR","visivel":true,"ordem":1,"configuracaoJson":"{}"}],
                          "historico":[{"publicoCodigo":"DIRETOR","codigoIndicador":"TOTAL_MATRICULAS","descricao":"Total de matriculas","valorAtual":120,"valorAnterior":118,"variacaoPercentual":1.69,"pontos":[{"referenciaData":"2026-07-15","valorNumeric":118,"valorTexto":"118"},{"referenciaData":"2026-07-16","valorNumeric":120,"valorTexto":"120"}]}]
                        }
                        """));

        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID filtroUsuarioId = UUID.randomUUID();

        mockMvc.perform(get("/internal/v1/dashboard/frontend")
                        .header("Authorization", "Bearer internal-token")
                        .header("X-Internal-Token", "dashboard-token")
                        .header("X-Correlation-Id", "corr-dashboard-frontend-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .param("publicoCodigo", "diretor")
                        .param("usuarioId", filtroUsuarioId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicoCodigo").value("DIRETOR"))
                .andExpect(jsonPath("$.dashboards[0].codigo").value("DASH_DIRETOR"))
                .andExpect(jsonPath("$.historico[0].codigoIndicador").value("TOTAL_MATRICULAS"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/api/dashboard/frontend?publicoCodigo=diretor&usuarioId=" + filtroUsuarioId);
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-dashboard-frontend-1");
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo(usuarioId.toString());
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
    }

    @Test
    void deveExigirTokenInternoValidoNaConsultaDePainelFrontend() throws Exception {
        mockMvc.perform(get("/internal/v1/dashboard/frontend")
                        .header("Authorization", "Bearer internal-token")
                        .header("X-Internal-Token", "token-invalido")
                        .header("X-Correlation-Id", "corr-dashboard-frontend-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .param("publicoCodigo", "diretor"))
                .andExpect(status().isUnauthorized());
    }
}

