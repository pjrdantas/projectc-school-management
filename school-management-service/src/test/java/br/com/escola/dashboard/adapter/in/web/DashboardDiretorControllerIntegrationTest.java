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
                "DELETE FROM transferencia_aluno",
                "DELETE FROM solicitacao_exclusao_aluno",
                "DELETE FROM matricula_documento_entregue",
                "DELETE FROM matricula_documento_exigido WHERE ordem = 33",
                "DELETE FROM matricula",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'dashboard.diretor.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'DASH-DIR-%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'DASH-DIR-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Dashboard Diretor%'",
                "DELETE FROM escola WHERE nome LIKE 'DASHBOARD-DIRETOR-%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-DIR-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-DIRETOR-%'",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'dashboard.diretor.%')",
                "DELETE FROM pessoa WHERE email LIKE 'dashboard.diretor.%'"
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
                "DELETE FROM transferencia_aluno",
                "DELETE FROM solicitacao_exclusao_aluno",
                "DELETE FROM matricula_documento_entregue",
                "DELETE FROM matricula_documento_exigido WHERE ordem = 33",
                "DELETE FROM matricula",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'dashboard.diretor.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'DASH-DIR-%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'DASH-DIR-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Dashboard Diretor%'",
                "DELETE FROM escola WHERE nome LIKE 'DASHBOARD-DIRETOR-%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-DIR-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-DIRETOR-%'",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'dashboard.diretor.%')",
                "DELETE FROM pessoa WHERE email LIKE 'dashboard.diretor.%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DashboardDiretorControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID STATUS_ALUNO_ATIVO_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
    private static final UUID TIPO_MATRICULA_PRIMEIRA_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000061");
    private static final UUID STATUS_MATRICULA_EM_ANDAMENTO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000072");
    private static final UUID STATUS_MATRICULA_EFETIVADA_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000075");
    private static final UUID TIPO_DOCUMENTO_HISTORICO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000095");
    private static final UUID TIPO_AVALIACAO_PROVA_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID TIPO_TRANSFERENCIA_ENTRADA_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATUS_TRANSFERENCIA_EM_ANDAMENTO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000112");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveConsultarDashboardDoDiretor() throws Exception {
        criarCenarioDiretor();

        mockMvc.perform(get("/api/dashboard/diretor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMatriculas").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.matriculasPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasEfetivadas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.alunosAtivos").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.alunosInativos").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.turmasAtivas").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.turmasLotadas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.professoresAlocados").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.aulasRealizadas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.avaliacoesRegistradas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.avaliacoesComNotasPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.transferencias").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.solicitacoesExclusaoPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasComDocumentosPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasPorStatus[?(@.status == 'EM_ANDAMENTO')].total")
                        .value(hasItem(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.turmasComVagas[?(@.turmaNome == 'Dashboard Diretor Turma B')].vagasDisponiveis")
                        .value(hasItem(4)));
    }

    private void criarCenarioDiretor() {
        UUID alunoAtivoId = criarAluno("Aluno Diretor Ativo", true);
        UUID alunoInativoId = criarAluno("Aluno Diretor Inativo", false);
        UUID professorId = criarProfessor();
        UUID periodoId = UUID.randomUUID();
        UUID turmaLotadaId = UUID.randomUUID();
        UUID turmaComVagaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO periodo_letivo (id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo, id_escola, created_at)
                VALUES (?, 'DASHBOARD-DIRETOR-2053.1', 2053, DATE '2053-02-01', DATE '2053-12-15', true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, periodoId);
        jdbcTemplate.update("""
                INSERT INTO turma (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, ativo, id_escola, created_at)
                VALUES (?, 'DASH-DIR-A', 'Dashboard Diretor Turma A', 1, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, turmaLotadaId, periodoId, SERIE_PADRAO_ID);
        jdbcTemplate.update("""
                INSERT INTO turma (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, ativo, id_escola, created_at)
                VALUES (?, 'DASH-DIR-B', 'Dashboard Diretor Turma B', 5, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, turmaComVagaId, periodoId, SERIE_PADRAO_ID);
        criarDocumentoExigidoPrimeiraMatricula();
        criarMatricula(alunoAtivoId, turmaLotadaId, periodoId, STATUS_MATRICULA_EFETIVADA_ID);
        criarMatricula(alunoInativoId, turmaComVagaId, periodoId, STATUS_MATRICULA_EM_ANDAMENTO_ID);
        jdbcTemplate.update("""
                INSERT INTO disciplina (id_disciplina, nome, carga_horaria, ativo, id_escola, created_at)
                VALUES (?, 'Dashboard Diretor Matematica', 80, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, disciplinaId);
        jdbcTemplate.update("""
                INSERT INTO turma_disciplina (id_turma_disciplina, id_turma, id_disciplina, carga_horaria, created_at)
                VALUES (?, ?, ?, 80, CURRENT_TIMESTAMP)
                """, turmaDisciplinaId, turmaLotadaId, disciplinaId);
        jdbcTemplate.update("""
                INSERT INTO professor_turma_disciplina (
                    id_professor_turma_disciplina, id_professor, id_turma_disciplina,
                    data_inicio, ativo, created_at
                ) VALUES (?, ?, ?, DATE '2053-02-01', true, CURRENT_TIMESTAMP)
                """, alocacaoId, professorId, turmaDisciplinaId);
        jdbcTemplate.update("""
                INSERT INTO aula (
                    id_aula, id_professor_turma_disciplina, data_aula,
                    conteudo_ministrado, realizada, created_at
                ) VALUES (?, ?, DATE '2053-03-10', 'Aula realizada para dashboard diretor', true, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alocacaoId);
        jdbcTemplate.update("""
                INSERT INTO avaliacao (
                    id_avaliacao, id_professor_turma_disciplina, id_tipo_avaliacao,
                    titulo, data_aplicacao, valor_maximo, peso, created_at
                ) VALUES (?, ?, ?, 'Avaliacao dashboard diretor', DATE '2053-03-20', 10.00, 1.00, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alocacaoId, TIPO_AVALIACAO_PROVA_ID);
        criarTransferencia(alunoAtivoId);
        criarSolicitacaoExclusaoPendente(alunoAtivoId);
    }

    private UUID criarAluno(String nome, boolean ativo) {
        UUID pessoaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES (?, ?, ?, ?, '00000000-0000-0000-0000-000000000047', ?, CURRENT_TIMESTAMP)
                """, pessoaId, nome, cpfAleatorio(), "dashboard.diretor.%s@example.com".formatted(alunoId), ativo);
        jdbcTemplate.update("""
                INSERT INTO aluno (id_aluno, id_pessoa, id_status_aluno, ra, emancipado, ativo, created_at)
                VALUES (?, ?, ?, ?, false, ?, CURRENT_TIMESTAMP)
                """, alunoId, pessoaId, STATUS_ALUNO_ATIVO_ID, "RA-DIR-" + System.nanoTime(), ativo);
        return alunoId;
    }

    private UUID criarProfessor() {
        UUID pessoaId = UUID.randomUUID();
        UUID cargoId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES (?, 'Professor Dashboard Diretor', ?, 'dashboard.diretor.professor@example.com', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """, pessoaId, cpfAleatorio());
        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES (?, ?, 'Professor')
                """, cargoId, "DASH-DIR-" + System.nanoTime());
        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
                """, funcionarioId, pessoaId, cargoId);
        jdbcTemplate.update("""
                INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at)
                VALUES (?, ?, 'RP-DASH-DIR', 'Licenciatura', true, CURRENT_TIMESTAMP)
                """, professorId, pessoaId);
        return professorId;
    }

    private void criarMatricula(UUID alunoId, UUID turmaId, UUID periodoId, UUID statusMatriculaId) {
        jdbcTemplate.update("""
                INSERT INTO matricula (
                    id_matricula, id_aluno, id_turma, id_periodo_letivo, id_status_matricula,
                    id_tipo_matricula, data_solicitacao, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, DATE '2053-01-15', CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alunoId, turmaId, periodoId, statusMatriculaId, TIPO_MATRICULA_PRIMEIRA_ID);
    }

    private void criarDocumentoExigidoPrimeiraMatricula() {
        jdbcTemplate.update("""
                INSERT INTO matricula_documento_exigido (
                    id_matricula_documento_exigido,
                    id_tipo_matricula,
                    id_tipo_documento,
                    obrigatorio,
                    ordem,
                    created_at
                ) VALUES (?, ?, ?, true, 33, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), TIPO_MATRICULA_PRIMEIRA_ID, TIPO_DOCUMENTO_HISTORICO_ID);
    }

    private void criarTransferencia(UUID alunoId) {
        UUID escolaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO escola (id_escola, nome, created_at)
                VALUES (?, 'DASHBOARD-DIRETOR-Escola Origem', CURRENT_TIMESTAMP)
                """, escolaId);
        jdbcTemplate.update("""
                INSERT INTO transferencia_aluno (
                    id_transferencia_aluno, id_aluno, id_escola_origem, id_tipo_transferencia,
                    id_status_transferencia, serie_origem, ano_letivo_origem, data_solicitacao,
                    motivo_transferencia, created_at
                ) VALUES (?, ?, ?, ?, ?, '1 ano', '2052', DATE '2053-01-20',
                    'Transferencia para dashboard diretor', CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alunoId, escolaId, TIPO_TRANSFERENCIA_ENTRADA_ID,
                STATUS_TRANSFERENCIA_EM_ANDAMENTO_ID);
    }

    private void criarSolicitacaoExclusaoPendente(UUID alunoId) {
        jdbcTemplate.update("""
                INSERT INTO solicitacao_exclusao_aluno (
                    id_solicitacao_exclusao_aluno,
                    id_aluno,
                    solicitado_por,
                    data_solicitacao,
                    motivo,
                    status
                ) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'Solicitacao para dashboard diretor', 'PENDENTE')
                """, UUID.randomUUID(), alunoId, UUID.randomUUID());
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
