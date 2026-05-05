package br.com.escola.enrollment.adapter.in.web;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM matricula",
                "DELETE FROM aluno_responsavel",
                "DELETE FROM turma",
                "DELETE FROM periodo_letivo",
                "DELETE FROM aluno"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MatriculaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void deveCriarMatriculaComStatusInicialAtiva() throws Exception {
        UUID alunoId = criarAluno();
        UUID periodoId = criarPeriodo("2026.3", "2026-02-01", "2026-06-30");
        UUID turmaId = criarTurma("TURMA-MAT-A", periodoId);

        String requestBody = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s"
                }
                """.formatted(alunoId, turmaId, periodoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoId.toString()))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    @WithMockUser
    void deveRetornarNotFoundQuandoAlunoNaoExistir() throws Exception {
        UUID periodoId = criarPeriodo("2026.4", "2026-08-01", "2026-12-20");
        UUID turmaId = criarTurma("TURMA-MAT-B", periodoId);

        String requestBody = """
                {
                  "alunoId": "00000000-0000-0000-0000-000000000001",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s"
                }
                """.formatted(turmaId, periodoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado para o id 00000000-0000-0000-0000-000000000001"));
    }

    @Test
    @WithMockUser
    void deveRetornarBadRequestQuandoTurmaNaoPertencerAoPeriodoInformado() throws Exception {
        UUID alunoId = criarAluno();
        UUID periodoTurma = criarPeriodo("2027.1", "2027-02-01", "2027-06-30");
        UUID periodoInvalido = criarPeriodo("2027.2", "2027-08-01", "2027-12-20");
        UUID turmaId = criarTurma("TURMA-MAT-C", periodoTurma);

        String requestBody = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s"
                }
                """.formatted(alunoId, turmaId, periodoInvalido);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A turma %s não pertence ao período letivo %s"
                        .formatted(turmaId, periodoInvalido)));
    }

    @Test
    @WithMockUser
    void deveFiltrarMatriculasPorAluno() throws Exception {
        UUID alunoAlvo = criarAluno();
        UUID outroAluno = criarAluno();
        UUID periodoId = criarPeriodo("2028.1", "2028-02-01", "2028-06-30");
        UUID turmaA = criarTurma("TURMA-FIL-ALUNO-A", periodoId);
        UUID turmaB = criarTurma("TURMA-FIL-ALUNO-B", periodoId);

        criarMatricula(alunoAlvo, turmaA, periodoId);
        criarMatricula(outroAluno, turmaB, periodoId);

        mockMvc.perform(get("/api/matriculas").param("alunoId", alunoAlvo.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].alunoId").value(alunoAlvo.toString()));
    }

    @Test
    @WithMockUser
    void deveFiltrarMatriculasPorTurma() throws Exception {
        UUID alunoA = criarAluno();
        UUID alunoB = criarAluno();
        UUID periodoId = criarPeriodo("2028.2", "2028-08-01", "2028-12-20");
        UUID turmaAlvo = criarTurma("TURMA-FIL-TURMA-A", periodoId);
        UUID outraTurma = criarTurma("TURMA-FIL-TURMA-B", periodoId);

        criarMatricula(alunoA, turmaAlvo, periodoId);
        criarMatricula(alunoB, outraTurma, periodoId);

        mockMvc.perform(get("/api/matriculas").param("turmaId", turmaAlvo.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].turmaId").value(turmaAlvo.toString()));
    }

    @Test
    @WithMockUser
    void deveFiltrarMatriculasPorPeriodoLetivo() throws Exception {
        UUID alunoA = criarAluno();
        UUID alunoB = criarAluno();
        UUID periodoAlvo = criarPeriodo("2029.1", "2029-02-01", "2029-06-30");
        UUID outroPeriodo = criarPeriodo("2029.2", "2029-08-01", "2029-12-20");
        UUID turmaAlvo = criarTurma("TURMA-FIL-PER-A", periodoAlvo);
        UUID turmaOutro = criarTurma("TURMA-FIL-PER-B", outroPeriodo);

        criarMatricula(alunoA, turmaAlvo, periodoAlvo);
        criarMatricula(alunoB, turmaOutro, outroPeriodo);

        mockMvc.perform(get("/api/matriculas").param("periodoLetivoId", periodoAlvo.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].periodoLetivoId").value(periodoAlvo.toString()));
    }

    @Test
    @WithMockUser
    void deveFiltrarMatriculasPorStatus() throws Exception {
        UUID alunoId = criarAluno();
        UUID periodoId = criarPeriodo("2030.1", "2030-02-01", "2030-06-30");
        UUID turmaId = criarTurma("TURMA-FIL-STATUS", periodoId);

        criarMatricula(alunoId, turmaId, periodoId);

        mockMvc.perform(get("/api/matriculas").param("status", "ATIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ATIVA"));
    }

    @Test
    @WithMockUser
    void deveRetornarBadRequestQuandoStatusForInvalido() throws Exception {
        mockMvc.perform(get("/api/matriculas").param("status", "XPT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status de matrícula inválido: XPT"));
    }

    private UUID criarAluno() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "Aluno Matricula",
                  "cpf": "%s",
                  "email": "matricula@example.com",
                  "dataNascimento": "2011-04-10"
                }
                """.formatted(cpfAleatorio());

        String responseBody = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarPeriodo(String nome, String dataInicio, String dataFim) throws Exception {
        String requestBody = """
                {
                  "nome": "%s",
                  "dataInicio": "%s",
                  "dataFim": "%s"
                }
                """.formatted(nome, dataInicio, dataFim);

        String responseBody = mockMvc.perform(post("/api/periodos-letivos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarTurma(String codigo, UUID periodoId) throws Exception {
        String requestBody = """
                {
                  "codigo": "%s",
                  "nome": "Turma de Matricula",
                  "capacidade": 40,
                  "periodoLetivoId": "%s"
                }
                """.formatted(codigo, periodoId);

        String responseBody = mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private void criarMatricula(UUID alunoId, UUID turmaId, UUID periodoId) throws Exception {
        String requestBody = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s"
                }
                """.formatted(alunoId, turmaId, periodoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}