package br.com.escola.planningaiservice.interfaces.rest;

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
class PlanningAiInternalControllerIntegrationTest {

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
        registry.add("planning-ai.internal-api.token", () -> "planning-token");
        registry.add("planning-ai.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveListarBibliotecaNoContratoInterno() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        UUID conteudoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "escolaId":"%s",
                            "escolaNome":"Escola Central",
                            "professorId":"%s",
                            "professorNome":"Professor Um",
                            "disciplinaId":"%s",
                            "disciplinaNome":"Matematica",
                            "tipoConteudo":"ATIVIDADE",
                            "tipoConteudoDescricao":"Atividade",
                            "titulo":"Lista",
                            "tema":"Fracoes",
                            "conteudo":"Conteudo gerado",
                            "origem":"PLANEJAMENTO_IA",
                            "reutilizavel":true,
                            "ativo":true,
                            "createdAt":"2026-07-13T10:15:30",
                            "updatedAt":"2026-07-13T10:15:30"
                          }
                        ]
                        """.formatted(conteudoId, UUID.randomUUID(), professorId, disciplinaId)));

        mockMvc.perform(get("/internal/v1/biblioteca-conteudos-pedagogicos")
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token")
                        .param("professorId", professorId.toString())
                        .param("disciplinaId", disciplinaId.toString())
                        .param("tipoConteudo", "ATIVIDADE")
                        .param("tema", "Fracoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(conteudoId.toString()))
                .andExpect(jsonPath("$[0].professorNome").value("Professor Um"))
                .andExpect(jsonPath("$[0].tipoConteudo").value("ATIVIDADE"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo(
                "/api/biblioteca-conteudos-pedagogicos?professorId=" + professorId
                        + "&disciplinaId=" + disciplinaId
                        + "&tipoConteudo=ATIVIDADE&tema=Fracoes");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
    }

    @Test
    void devePropagarNotFoundQuandoTipoConteudoNaoExiste() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Tipo de conteudo nao encontrado"
                        }
                        """));

        mockMvc.perform(get("/internal/v1/biblioteca-conteudos-pedagogicos")
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token")
                        .param("tipoConteudo", "INVALIDO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/biblioteca-conteudos-pedagogicos?tipoConteudo=INVALIDO");
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/biblioteca-conteudos-pedagogicos")
                        .header("X-Correlation-Id", "corr-planning-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveListarInteracoesNoContratoInterno() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "planejamentoBimestralId":"%s",
                            "escolaId":"%s",
                            "escolaNome":"Escola Central",
                            "promptProfessor":"Monte uma atividade sobre fracoes",
                            "respostaIA":"Sugestao de atividade",
                            "modeloIA":"gpt-4.1",
                            "tokensEntrada":120,
                            "tokensSaida":340,
                            "custoEstimado":1.25,
                            "createdAt":"2026-07-13T11:00:00"
                          }
                        ]
                        """.formatted(interacaoId, planejamentoId, UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/interacoes", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(interacaoId.toString()))
                .andExpect(jsonPath("$[0].planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$[0].modeloIA").value("gpt-4.1"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/planejamentos-bimestrais/" + planejamentoId + "/ia/interacoes");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
    }

    @Test
    void devePropagarNotFoundQuandoPlanejamentoNaoExisteNasInteracoes() throws Exception {
        UUID planejamentoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Planejamento bimestral nao encontrado"
                        }
                        """));

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/interacoes", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-5")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/planejamentos-bimestrais/" + planejamentoId + "/ia/interacoes");
    }

    private RecordedRequest aguardarRequisicao() throws InterruptedException {
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        return recorded;
    }
}
