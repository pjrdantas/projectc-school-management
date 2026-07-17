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
class PainelIndicadorSnapshotInternalControllerIntegrationTest {

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
    void deveConsultarSnapshotsPorPublicoNoContratoInterno() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
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

        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        mockMvc.perform(get("/internal/v1/dashboard/snapshots/publicos/{publicoCodigo}", "dash-snap-diretor")
                        .header("Authorization", "Bearer internal-token")
                        .header("X-Internal-Token", "dashboard-token")
                        .header("X-Correlation-Id", "corr-dashboard-snapshot-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .param("referenciaData", "2026-07-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicoCodigo").value("DASH-SNAP-DIRETOR"))
                .andExpect(jsonPath("$[0].codigoIndicador").value("TOTAL_MATRICULAS"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/api/dashboard/snapshots/publicos/dash-snap-diretor?referenciaData=2026-07-16");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-dashboard-snapshot-1");
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo(usuarioId.toString());
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
    }

    @Test
    void deveExigirTokenInternoValidoNaConsultaDeSnapshots() throws Exception {
        mockMvc.perform(get("/internal/v1/dashboard/snapshots/publicos/{publicoCodigo}", "dash-snap-diretor")
                        .header("Authorization", "Bearer internal-token")
                        .header("X-Internal-Token", "token-invalido")
                        .header("X-Correlation-Id", "corr-dashboard-snapshot-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}

