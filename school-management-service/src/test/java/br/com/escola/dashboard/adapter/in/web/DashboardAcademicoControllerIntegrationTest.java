package br.com.escola.dashboard.adapter.in.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM boletim_item",
                "DELETE FROM boletim",
                "DELETE FROM historico_escolar WHERE observacoes LIKE 'DASHBOARD-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM disciplina WHERE nome LIKE 'DASHBOARD-%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM boletim_item",
                "DELETE FROM boletim",
                "DELETE FROM historico_escolar WHERE observacoes LIKE 'DASHBOARD-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM disciplina WHERE nome LIKE 'DASHBOARD-%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DashboardAcademicoControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveConsultarDashboardAcademicoOperacional() throws Exception {
        UUID alunoId = criarAluno("Aluno Dashboard", cpfAleatorio(), "aluno.dashboard.%s@example.com".formatted(System.nanoTime()));
        UUID periodoId = criarPeriodo("DASHBOARD-2050.1", "2050-02-01", "2050-12-15");
        UUID turmaId = criarTurma("DASH-TURMA-A", "Dashboard Turma A", 5, periodoId);
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaId, "CONCLUIDA", "Conclusao para dashboard");
        UUID disciplinaId = criarDisciplina("DASHBOARD-Matematica", 80);
        criarBoletimFechado(matriculaId, disciplinaId, "APROVADO");
        criarHistoricoInterno(alunoId);

        mockMvc.perform(get("/api/dashboard/academico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMatriculas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasConcluidas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasAptasRematricula").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.boletinsFechados").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.historicosInternosGerados").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.alunosAprovados").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasPorStatus[?(@.status == 'CONCLUIDA')].total")
                        .value(hasItem(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.turmasComVagas[?(@.turmaNome == 'Dashboard Turma A')].vagasDisponiveis")
                        .value(hasItem(4)));
    }

    private UUID criarAluno(String nome, String cpf, String email) throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "%s",
                  "cpf": "%s",
                  "email": "%s",
                  "dataNascimento": "2010-05-15"
                }
                """.formatted(nome, cpf, email);

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

    private UUID criarTurma(String codigo, String nome, int capacidade, UUID periodoId) throws Exception {
        String requestBody = """
                {
                  "codigo": "%s",
                  "nome": "%s",
                  "capacidade": %d,
                  "periodoLetivoId": "%s",
                  "serieId": "%s",
                  "turno": "MANHA",
                  "status": "ATIVA"
                }
                """.formatted(codigo, nome, capacidade, periodoId, SERIE_PADRAO_ID);

        String responseBody = mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarMatricula(UUID alunoId, UUID turmaId, UUID periodoId) throws Exception {
        String requestBody = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s",
                  "tipoMatricula": "PRIMEIRA_MATRICULA"
                }
                """.formatted(alunoId, turmaId, periodoId);

        String responseBody = mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private void atualizarStatusMatricula(UUID matriculaId, String novoStatus, String justificativa) throws Exception {
        String requestBody = """
                {
                  "status": "%s",
                  "justificativa": "%s"
                }
                """.formatted(novoStatus, justificativa);

        mockMvc.perform(patch("/api/matriculas/{id}/status", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(novoStatus));
    }

    private UUID criarDisciplina(String nome, int cargaHoraria) {
        UUID disciplinaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO disciplina (id_disciplina, nome, carga_horaria, ativo, id_escola, created_at)
                VALUES (?, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, disciplinaId, nome, cargaHoraria);
        return disciplinaId;
    }

    private void criarBoletimFechado(UUID matriculaId, UUID disciplinaId, String resultado) {
        UUID boletimId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO boletim (id_boletim, id_matricula, periodo_referencia, data_fechamento, created_at)
                VALUES (?, ?, '2050.1', CURRENT_DATE, CURRENT_TIMESTAMP)
                """, boletimId, matriculaId);
        jdbcTemplate.update("""
                INSERT INTO boletim_item (
                    id_boletim_item, id_boletim, id_disciplina, media,
                    frequencia_percentual, resultado, carga_horaria
                ) VALUES (?, ?, ?, 8.50, 100.00, ?, 80)
                """, UUID.randomUUID(), boletimId, disciplinaId, resultado);
    }

    private void criarHistoricoInterno(UUID alunoId) {
        jdbcTemplate.update("""
                INSERT INTO historico_escolar (
                    id_historico_escolar, id_aluno, origem, ano_conclusao,
                    ensino_concluido, data_emissao, observacoes, created_at
                ) VALUES (?, ?, 'INTERNO', 2050, 'Ensino Fundamental', CURRENT_DATE, 'DASHBOARD-HISTORICO', CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alunoId);
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
