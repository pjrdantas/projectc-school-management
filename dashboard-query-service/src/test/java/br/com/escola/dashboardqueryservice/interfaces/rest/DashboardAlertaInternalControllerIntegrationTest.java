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
class DashboardAlertaInternalControllerIntegrationTest {

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
    void deveConsultarAlertasNoContratoInterno() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [{
                          "publicoCodigo":"DIRETOR",
                          "professorId":null,
                          "codigo":"TURMAS_LOTADAS",
                          "severidade":"CRITICO",
                          "titulo":"Turmas lotadas",
                          "mensagem":"Existem turmas sem vagas disponíveis.",
                          "valor":2,
                          "limite":0
                        }]
                        """));

        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        mockMvc.perform(get("/internal/v1/dashboard/alertas")
                        .param("publicoCodigo", "diretor")
                        .header("X-Internal-Token", "dashboard-token")
                        .header("X-Correlation-Id", "corr-dashboard-alerta-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer dashboard-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicoCodigo").value("DIRETOR"))
                .andExpect(jsonPath("$[0].codigo").value("TURMAS_LOTADAS"));

        RecordedRequest recorded = aguardarRequisicao("/api/dashboard/alertas?publicoCodigo=diretor");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer dashboard-token");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-dashboard-alerta-1");
        assertThat(recorded.getHeader("X-Usuario-Id")).isEqualTo(usuarioId.toString());
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
    }

    @Test
    void deveConsultarAlertasDeProfessorComProfessorIdNoContratoInterno() throws Exception {
        UUID professorId = UUID.fromString("00000000-0000-0000-0000-000000000401");

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [{
                          "publicoCodigo":"PROFESSOR",
                          "professorId":"00000000-0000-0000-0000-000000000401",
                          "codigo":"FREQUENCIAS_PENDENTES",
                          "severidade":"CRITICO",
                          "titulo":"Frequências pendentes",
                          "mensagem":"Existem aulas realizadas sem registro de frequência do professor.",
                          "valor":1,
                          "limite":0
                        }]
                        """));

        mockMvc.perform(get("/internal/v1/dashboard/alertas")
                        .param("publicoCodigo", "professor")
                        .param("professorId", professorId.toString())
                        .header("X-Internal-Token", "dashboard-token")
                        .header("X-Correlation-Id", "corr-dashboard-alerta-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer dashboard-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].codigo").value("FREQUENCIAS_PENDENTES"));

        RecordedRequest recorded = aguardarRequisicao(
                "/api/dashboard/alertas?publicoCodigo=professor&professorId=" + professorId);
        assertThat(recorded).isNotNull();
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/dashboard/alertas")
                        .param("publicoCodigo", "diretor")
                        .header("X-Correlation-Id", "corr-dashboard-alerta-3")
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
