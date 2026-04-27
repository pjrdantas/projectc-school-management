package br.com.escola.enrollment.adapter.in.web;

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

    @SuppressWarnings("null")
	@Test
    @WithMockUser
    void deveCriarMatriculaComStatusInicialAtiva() throws Exception {
        Long alunoId = criarAluno();
        Long periodoId = criarPeriodo("2026.3", "2026-02-01", "2026-06-30");
        Long turmaId = criarTurma("TURMA-MAT-A", periodoId);

        String requestBody = """
                {
                  "alunoId": %d,
                  "turmaId": %d,
                  "periodoLetivoId": %d
                }
                """.formatted(alunoId, turmaId, periodoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId))
                .andExpect(jsonPath("$.turmaId").value(turmaId))
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoId))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @SuppressWarnings("null")
	@Test
    @WithMockUser
    void deveRetornarNotFoundQuandoAlunoNaoExistir() throws Exception {
        Long periodoId = criarPeriodo("2026.4", "2026-08-01", "2026-12-20");
        Long turmaId = criarTurma("TURMA-MAT-B", periodoId);

        String requestBody = """
                {
                  "alunoId": 99999,
                  "turmaId": %d,
                  "periodoLetivoId": %d
                }
                """.formatted(turmaId, periodoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado para o id 99999"));
    }

    @SuppressWarnings("null")
	@Test
    @WithMockUser
    void deveRetornarBadRequestQuandoTurmaNaoPertencerAoPeriodoInformado() throws Exception {
        Long alunoId = criarAluno();
        Long periodoTurma = criarPeriodo("2027.1", "2027-02-01", "2027-06-30");
        Long periodoInvalido = criarPeriodo("2027.2", "2027-08-01", "2027-12-20");
        Long turmaId = criarTurma("TURMA-MAT-C", periodoTurma);

        String requestBody = """
                {
                  "alunoId": %d,
                  "turmaId": %d,
                  "periodoLetivoId": %d
                }
                """.formatted(alunoId, turmaId, periodoInvalido);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A turma %d não pertence ao período letivo %d"
                        .formatted(turmaId, periodoInvalido)));
    }

    @Test
    @WithMockUser
    void deveFiltrarMatriculasPorAluno() throws Exception {
        Long alunoAlvo = criarAluno();
        Long outroAluno = criarAluno();
        Long periodoId = criarPeriodo("2028.1", "2028-02-01", "2028-06-30");
        Long turmaA = criarTurma("TURMA-FIL-ALUNO-A", periodoId);
        Long turmaB = criarTurma("TURMA-FIL-ALUNO-B", periodoId);

        criarMatricula(alunoAlvo, turmaA, periodoId);
        criarMatricula(outroAluno, turmaB, periodoId);

        mockMvc.perform(get("/api/matriculas").param("alunoId", alunoAlvo.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].alunoId").value(alunoAlvo));
    }

    @Test
    @WithMockUser
    void deveFiltrarMatriculasPorTurma() throws Exception {
        Long alunoA = criarAluno();
        Long alunoB = criarAluno();
        Long periodoId = criarPeriodo("2028.2", "2028-08-01", "2028-12-20");
        Long turmaAlvo = criarTurma("TURMA-FIL-TURMA-A", periodoId);
        Long outraTurma = criarTurma("TURMA-FIL-TURMA-B", periodoId);

        criarMatricula(alunoA, turmaAlvo, periodoId);
        criarMatricula(alunoB, outraTurma, periodoId);

        mockMvc.perform(get("/api/matriculas").param("turmaId", turmaAlvo.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].turmaId").value(turmaAlvo));
    }

    @Test
    @WithMockUser
    void deveFiltrarMatriculasPorPeriodoLetivo() throws Exception {
        Long alunoA = criarAluno();
        Long alunoB = criarAluno();
        Long periodoAlvo = criarPeriodo("2029.1", "2029-02-01", "2029-06-30");
        Long outroPeriodo = criarPeriodo("2029.2", "2029-08-01", "2029-12-20");
        Long turmaAlvo = criarTurma("TURMA-FIL-PER-A", periodoAlvo);
        Long turmaOutro = criarTurma("TURMA-FIL-PER-B", outroPeriodo);

        criarMatricula(alunoA, turmaAlvo, periodoAlvo);
        criarMatricula(alunoB, turmaOutro, outroPeriodo);

        mockMvc.perform(get("/api/matriculas").param("periodoLetivoId", periodoAlvo.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].periodoLetivoId").value(periodoAlvo));
    }

    @Test
    @WithMockUser
    void deveFiltrarMatriculasPorStatus() throws Exception {
        Long alunoId = criarAluno();
        Long periodoId = criarPeriodo("2030.1", "2030-02-01", "2030-06-30");
        Long turmaId = criarTurma("TURMA-FIL-STATUS", periodoId);

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

    private Long criarAluno() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "Aluno Matricula",
                  "cpf": "%s",
                  "email": "matricula@example.com",
                  "dataNascimento": "2011-04-10"
                }
                """.formatted(cpfAleatorio());

        @SuppressWarnings("null")
		String responseBody = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asLong();
    }

    private Long criarPeriodo(String nome, String dataInicio, String dataFim) throws Exception {
        String requestBody = """
                {
                  "nome": "%s",
                  "dataInicio": "%s",
                  "dataFim": "%s"
                }
                """.formatted(nome, dataInicio, dataFim);

        @SuppressWarnings("null")
		String responseBody = mockMvc.perform(post("/api/periodos-letivos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asLong();
    }

    private Long criarTurma(String codigo, Long periodoId) throws Exception {
        String requestBody = """
                {
                  "codigo": "%s",
                  "nome": "Turma de Matricula",
                  "capacidade": 40,
                  "periodoLetivoId": %d
                }
                """.formatted(codigo, periodoId);

        @SuppressWarnings("null")
		String responseBody = mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asLong();
    }

    @SuppressWarnings("null")
	private void criarMatricula(Long alunoId, Long turmaId, Long periodoId) throws Exception {
        String requestBody = """
                {
                  "alunoId": %d,
                  "turmaId": %d,
                  "periodoLetivoId": %d
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