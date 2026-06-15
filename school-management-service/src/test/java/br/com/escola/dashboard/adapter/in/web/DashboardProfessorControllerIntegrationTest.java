package br.com.escola.dashboard.adapter.in.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "MERGE INTO tipo_avaliacao (id_tipo_avaliacao, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000301', 'PROVA', 'Prova')",
                "DELETE FROM nota_aluno",
                "DELETE FROM avaliacao",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM planejamento_bimestral",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'dashboard.professor.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'DASH-PROF-%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'DASH-PROF-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Dashboard Professor%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-PROF-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-PROFESSOR-%'",
                "DELETE FROM pessoa WHERE email LIKE 'dashboard.professor.%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM nota_aluno",
                "DELETE FROM avaliacao",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM planejamento_bimestral",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'dashboard.professor.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'DASH-PROF-%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'DASH-PROF-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Dashboard Professor%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-PROF-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-PROFESSOR-%'",
                "DELETE FROM pessoa WHERE email LIKE 'dashboard.professor.%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DashboardProfessorControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID TIPO_AVALIACAO_PROVA_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveConsultarDashboardDoProfessor() throws Exception {
        UUID professorId = criarCenarioProfessor();

        mockMvc.perform(get("/api/dashboard/professores/{professorId}", professorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value("00000000-0000-0000-0000-000000000047"))
                .andExpect(jsonPath("$.escolaNome").value("Escola padrão"))
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.turmasVinculadas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.alocacoesAtivas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.aulasPlanejadas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.aulasRealizadas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.frequenciasPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.avaliacoesRegistradas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.avaliacoesComNotasPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.planejamentosBimestrais").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.planejamentosBimestraisPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.turmas[?(@.turmaNome == 'Dashboard Professor Turma A')].disciplinaNome")
                        .value(hasItem("Dashboard Professor Matematica")));
    }

    private UUID criarCenarioProfessor() {
        UUID pessoaId = UUID.randomUUID();
        UUID cargoId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        UUID periodoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES (?, 'Professor Dashboard', ?, 'dashboard.professor.docente@example.com', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """, pessoaId, cpfAleatorio());
        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES (?, ?, 'Professor')
                """, cargoId, "DASH-PROF-" + System.nanoTime());
        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
                """, funcionarioId, pessoaId, cargoId);
        jdbcTemplate.update("""
                INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at)
                VALUES (?, ?, 'RP-DASH-PROF', 'Licenciatura', true, CURRENT_TIMESTAMP)
                """, professorId, pessoaId);
        jdbcTemplate.update("""
                INSERT INTO periodo_letivo (id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo, id_escola, created_at)
                VALUES (?, 'DASHBOARD-PROFESSOR-2052.1', 2052, DATE '2052-02-01', DATE '2052-12-15', true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, periodoId);
        jdbcTemplate.update("""
                INSERT INTO turma (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, ativo, id_escola, created_at)
                VALUES (?, 'DASH-PROF-A', 'Dashboard Professor Turma A', 30, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, turmaId, periodoId, SERIE_PADRAO_ID);
        jdbcTemplate.update("""
                INSERT INTO disciplina (id_disciplina, nome, carga_horaria, ativo, id_escola, created_at)
                VALUES (?, 'Dashboard Professor Matematica', 80, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, disciplinaId);
        jdbcTemplate.update("""
                INSERT INTO turma_disciplina (id_turma_disciplina, id_turma, id_disciplina, carga_horaria, created_at)
                VALUES (?, ?, ?, 80, CURRENT_TIMESTAMP)
                """, turmaDisciplinaId, turmaId, disciplinaId);
        jdbcTemplate.update("""
                INSERT INTO professor_turma_disciplina (
                    id_professor_turma_disciplina, id_professor, id_turma_disciplina,
                    data_inicio, ativo, created_at
                ) VALUES (?, ?, ?, DATE '2052-02-01', true, CURRENT_TIMESTAMP)
                """, alocacaoId, professorId, turmaDisciplinaId);
        jdbcTemplate.update("""
                INSERT INTO aula (
                    id_aula, id_professor_turma_disciplina, data_aula,
                    conteudo_ministrado, realizada, created_at
                ) VALUES (?, ?, DATE '2052-03-10', 'Aula planejada', false, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alocacaoId);
        jdbcTemplate.update("""
                INSERT INTO aula (
                    id_aula, id_professor_turma_disciplina, data_aula,
                    conteudo_ministrado, realizada, created_at
                ) VALUES (?, ?, DATE '2052-03-11', 'Aula realizada sem frequencia', true, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alocacaoId);
        jdbcTemplate.update("""
                INSERT INTO avaliacao (
                    id_avaliacao, id_professor_turma_disciplina, id_tipo_avaliacao,
                    titulo, data_aplicacao, valor_maximo, peso, created_at
                ) VALUES (?, ?, ?, 'Avaliacao dashboard professor', DATE '2052-03-20', 10.00, 1.00, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alocacaoId, TIPO_AVALIACAO_PROVA_ID);
        jdbcTemplate.update("""
                INSERT INTO planejamento_bimestral (
                    id_planejamento_bimestral, id_professor_turma_disciplina,
                    titulo, tema_principal, descricao_inicial,
                    reutilizavel, criado_com_auxilio_ia, aprovado_pelo_professor, created_at
                ) VALUES (?, ?, 'Planejamento dashboard', ?, 'Planejamento usado no dashboard do professor',
                    false, false, false, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alocacaoId, "Dashboard Professor Tema " + System.nanoTime());

        return professorId;
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
