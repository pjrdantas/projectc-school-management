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
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aluno.dashboard.outra.%')",
                "DELETE FROM pessoa WHERE email LIKE 'aluno.dashboard.outra.%'",
                "DELETE FROM disciplina WHERE nome LIKE 'DASHBOARD-%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-%'",
                "DELETE FROM escola WHERE nome LIKE 'DASHBOARD-ACADEMICO-OUTRA-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM boletim_item",
                "DELETE FROM boletim",
                "DELETE FROM historico_escolar WHERE observacoes LIKE 'DASHBOARD-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aluno.dashboard.outra.%')",
                "DELETE FROM pessoa WHERE email LIKE 'aluno.dashboard.outra.%'",
                "DELETE FROM disciplina WHERE nome LIKE 'DASHBOARD-%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-%'",
                "DELETE FROM escola WHERE nome LIKE 'DASHBOARD-ACADEMICO-OUTRA-%'"
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
        criarCenarioOutraEscolaIgnorado();

        mockMvc.perform(get("/api/dashboard/academico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value("00000000-0000-0000-0000-000000000047"))
                .andExpect(jsonPath("$.escolaNome").value("Escola padrão"))
                .andExpect(jsonPath("$.totalMatriculas").value(1))
                .andExpect(jsonPath("$.matriculasConcluidas").value(1))
                .andExpect(jsonPath("$.matriculasAptasRematricula").value(1))
                .andExpect(jsonPath("$.boletinsFechados").value(1))
                .andExpect(jsonPath("$.historicosInternosGerados").value(1))
                .andExpect(jsonPath("$.alunosAprovados").value(1))
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
                    id_historico_escolar, id_aluno, origem, nome_aluno, status, bloqueado,
                    ano_conclusao, ensino_concluido, data_emissao, observacoes, created_at
                )
                SELECT ?, a.id_aluno, 'INTERNO', p.nome_completo, 'PENDENTE', false,
                       2050, 'Ensino Fundamental', CURRENT_DATE, 'DASHBOARD-HISTORICO', CURRENT_TIMESTAMP
                  FROM aluno a
                  JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                 WHERE a.id_aluno = ?
                """, UUID.randomUUID(), alunoId);
    }

    private void criarCenarioOutraEscolaIgnorado() {
        UUID escolaId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID periodoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO escola (id_escola, nome, ativo, created_at)
                VALUES (?, 'DASHBOARD-ACADEMICO-OUTRA-ESCOLA', true, CURRENT_TIMESTAMP)
                """, escolaId);
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES (?, 'Aluno Dashboard Outra Escola', ?, ?, ?, true, CURRENT_TIMESTAMP)
                """, pessoaId, cpfAleatorio(), "aluno.dashboard.outra.%s@example.com".formatted(alunoId), escolaId);
        jdbcTemplate.update("""
                INSERT INTO aluno (id_aluno, id_pessoa, id_status_aluno, ra, emancipado, ativo, created_at)
                VALUES (?, ?, '00000000-0000-0000-0000-000000000021', ?, false, true, CURRENT_TIMESTAMP)
                """, alunoId, pessoaId, "RA-OUTRA-" + System.nanoTime());
        jdbcTemplate.update("""
                INSERT INTO periodo_letivo (id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo, id_escola, created_at)
                VALUES (?, 'DASHBOARD-OUTRA-2050.1', 2050, DATE '2050-02-01', DATE '2050-12-15', true, ?, CURRENT_TIMESTAMP)
                """, periodoId, escolaId);
        jdbcTemplate.update("""
                INSERT INTO turma (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, ativo, id_escola, created_at)
                VALUES (?, 'DASH-OUTRA-A', 'Dashboard Outra Escola Turma A', 5, ?, ?, true, ?, CURRENT_TIMESTAMP)
                """, turmaId, periodoId, SERIE_PADRAO_ID, escolaId);
        jdbcTemplate.update("""
                INSERT INTO matricula (
                    id_matricula, id_aluno, id_turma, id_periodo_letivo,
                    id_status_matricula, id_tipo_matricula, data_solicitacao, created_at
                ) VALUES (
                    ?, ?, ?, ?,
                    (SELECT id_status_matricula FROM status_matricula WHERE codigo = 'CONCLUIDA'),
                    (SELECT id_tipo_matricula FROM tipo_matricula WHERE codigo = 'PRIMEIRA_MATRICULA'),
                    DATE '2050-01-15', CURRENT_TIMESTAMP
                )
                """, matriculaId, alunoId, turmaId, periodoId);
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
