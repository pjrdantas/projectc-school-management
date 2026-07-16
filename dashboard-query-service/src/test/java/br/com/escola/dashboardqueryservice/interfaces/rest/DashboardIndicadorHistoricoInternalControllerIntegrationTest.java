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
class DashboardIndicadorHistoricoInternalControllerIntegrationTest {

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
    void deveConsultarHistoricoPorPublicoNoContratoInterno() throws Exception {
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
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

        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        mockMvc.perform(get("/internal/v1/dashboard/snapshots/historico/publicos/{publicoCodigo}", "dash-snap-professor")
                        .header("Authorization", "Bearer internal-token")
                        .header("X-Internal-Token", "dashboard-token")
                        .header("X-Correlation-Id", "corr-dashboard-historico-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .param("codigoIndicador", "aulas_realizadas")
                        .param("dataInicio", "2026-07-15")
                        .param("dataFim", "2026-07-16")
                        .param("professorId", professorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicoCodigo").value("DASH-SNAP-PROFESSOR"))
                .andExpect(jsonPath("$[0].codigoIndicador").value("PROFESSOR_INDICADOR"))
                .andExpect(jsonPath("$[0].pontos[1].referenciaData").value("2026-07-16"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo(
                "/api/dashboard/snapshots/historico/publicos/dash-snap-professor?codigoIndicador=aulas_realizadas&dataInicio=2026-07-15&dataFim=2026-07-16&professorId="
                        + professorId);
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-dashboard-historico-1");
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo(usuarioId.toString());
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
    }

    @Test
    void deveExigirTokenInternoValidoNaConsultaDeHistorico() throws Exception {
        mockMvc.perform(get("/internal/v1/dashboard/snapshots/historico/publicos/{publicoCodigo}", "dash-snap-professor")
                        .header("Authorization", "Bearer internal-token")
                        .header("X-Internal-Token", "token-invalido")
                        .header("X-Correlation-Id", "corr-dashboard-historico-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
