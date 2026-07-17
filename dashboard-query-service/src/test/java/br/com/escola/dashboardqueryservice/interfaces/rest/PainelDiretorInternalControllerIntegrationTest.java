package br.com.escola.dashboardqueryservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
class PainelDiretorInternalControllerIntegrationTest {

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
    void deveConsultarPainelDiretorNoContratoInterno() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
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
                          "turmasComVagas":[{"turmaId":"00000000-0000-0000-0000-000000000303","turmaNome":"Turma Diretor","capacidade":30,"vagasOcupadas":20,"vagasDisponiveis":10}]
                        }
                        """));

        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        mockMvc.perform(get("/internal/v1/dashboard/diretor")
                        .header("X-Internal-Token", "dashboard-token")
                        .header("X-Correlation-Id", "corr-dashboard-dir-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer dashboard-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value("00000000-0000-0000-0000-000000000047"))
                .andExpect(jsonPath("$.matriculasPendentes").value(2))
                .andExpect(jsonPath("$.avaliacoesRegistradas").value(5))
                .andExpect(jsonPath("$.matriculasPorStatus[0].status").value("EM_ANDAMENTO"));

        RecordedRequest recorded = aguardarRequisicao("/api/dashboard/diretor");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer dashboard-token");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-dashboard-dir-1");
        assertThat(recorded.getHeader("X-Usuario-Id")).isEqualTo(usuarioId.toString());
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/dashboard/diretor")
                        .header("X-Correlation-Id", "corr-dashboard-dir-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer dashboard-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    private RecordedRequest aguardarRequisicao(String path) throws InterruptedException {
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getPath()).isEqualTo(path);
        return recorded;
    }
}

