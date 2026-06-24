package br.com.escola.professorservice.interfaces.rest;

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
class ProfessorShadowQueryControllerIntegrationTest {

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
        registry.add("professor.shadow.internal-api.token", () -> "shadow-token");
        registry.add("professor.shadow.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveConsultarProfessorPorIdNoRuntimeShadow() throws Exception {
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Shadow",
                          "escolaId": "00000000-0000-0000-0000-000000000047",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-SHADOW",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-23T10:15:30",
                          "updatedAt": "2026-06-23T10:15:30"
                        }
                        """.formatted(professorId, UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Shadow"));

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("GET");
        assertThat(recorded.getPath()).isEqualTo("/internal/professores/" + professorId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer shadow-user-token");
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-shadow-1");
    }

    @Test
    void deveListarProfessoresAlocacoesEFuncionariosElegiveisNoRuntimeShadow() throws Exception {
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Professor Shadow Lista",
                            "escolaId": "00000000-0000-0000-0000-000000000047",
                            "escolaNome": "Escola Padrao",
                            "registroProfissional": "RP-LISTA",
                            "formacao": "Licenciatura",
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30",
                            "updatedAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "professorId": "%s",
                            "professorNome": "Professor Shadow",
                            "turmaDisciplinaId": "%s",
                            "turmaId": "%s",
                            "turmaNome": "1A",
                            "disciplinaId": "%s",
                            "disciplinaNome": "Matematica",
                            "dataInicio": "2026-02-01",
                            "dataFim": null,
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(
                        UUID.randomUUID(),
                        professorId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "funcionarioId": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Funcionario Elegivel Shadow",
                            "escolaId": "00000000-0000-0000-0000-000000000047",
                            "escolaNome": "Escola Padrao",
                            "cargo": "Professor",
                            "ativo": true,
                            "elegivelProfessor": true
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-2a")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor Shadow Lista"));

        mockMvc.perform(get("/internal/v1/professores/{id}/turmas-disciplinas", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Matematica"));

        mockMvc.perform(get("/internal/v1/professores/funcionarios-elegiveis")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Funcionario Elegivel Shadow"))
                .andExpect(jsonPath("$[0].elegivelProfessor").value(true));
    }

    @Test
    void deveListarProfessoresPorTurmaNoRuntimeShadow() throws Exception {
        UUID turmaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "professorId": "%s",
                            "professorNome": "Professor Shadow Turma",
                            "turmaDisciplinaId": "%s",
                            "turmaId": "%s",
                            "turmaNome": "1B",
                            "disciplinaId": "%s",
                            "disciplinaNome": "Historia",
                            "dataInicio": "2026-02-01",
                            "dataFim": null,
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(
                        UUID.randomUUID(),
                        professorId,
                        UUID.randomUUID(),
                        turmaId,
                        UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/turmas/{turmaId}/professores", turmaId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-3b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Historia"));

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getPath()).isEqualTo("/internal/professores/turmas/" + turmaId);
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/professores/funcionarios-elegiveis")
                        .header("X-Correlation-Id", "corr-shadow-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveMapearProfessorNaoEncontrado() throws Exception {
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-5")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }
}
