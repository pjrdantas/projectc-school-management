package br.com.escola.professor.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import br.com.escola.professor.application.dto.internal.AlocarProfessorTurmaDisciplinaSolicitacao;
import br.com.escola.professor.application.dto.internal.CriarProfessorSolicitacao;
import br.com.escola.professor.application.port.internal.ProfessorAcademicoPort;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaDuplicadaException;

@SpringBootTest
@Sql(
        statements = {
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'professor.internal.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PROF-INTERNAL-%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor.internal.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PROF-INT-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Professor Internal %'",
                "DELETE FROM turma WHERE codigo LIKE 'PROF-INT-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PROF-INT-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'professor.internal.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PROF-INTERNAL-%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor.internal.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PROF-INT-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Professor Internal %'",
                "DELETE FROM turma WHERE codigo LIKE 'PROF-INT-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PROF-INT-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProfessorAcademicoInternalServiceIntegrationTest {

    private static final UUID ESCOLA_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID TURNO_MANHA_ID = UUID.fromString("00000000-0000-0000-0000-000000000051");

    @Autowired
    private ProfessorAcademicoPort professorAcademicoPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCriarProfessorEAlocarViaContratoInterno() {
        UUID funcionarioId = criarFuncionario("Professor Internal Fluxo", "professor.internal.fluxo@example.com");
        UUID periodoId = criarPeriodo("PROF-INT-2038.1", "2038-02-01", "2038-06-30");
        UUID turmaId = criarTurma("PROF-INT-A", "Professor Internal Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Professor Internal Matematica", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);

        var professor = professorAcademicoPort.criarProfessor(
                ESCOLA_PADRAO_ID,
                new CriarProfessorSolicitacao(
                        funcionarioId,
                        "RP-2038",
                        "Licenciatura em Matemática",
                        null));

        assertThat(professor.nomeCompleto()).isEqualTo("Professor Internal Fluxo");
        assertThat(professor.registroProfissional()).isEqualTo("RP-2038");
        assertThat(professor.ativo()).isTrue();
        assertThat(professorAcademicoPort.buscarProfessor(ESCOLA_PADRAO_ID, professor.id())).isPresent();

        var alocacao = professorAcademicoPort.alocarProfessorTurmaDisciplina(
                ESCOLA_PADRAO_ID,
                professor.id(),
                new AlocarProfessorTurmaDisciplinaSolicitacao(
                        turmaDisciplinaId,
                        java.time.LocalDate.parse("2038-02-01"),
                        null,
                        null));

        assertThat(alocacao.professorId()).isEqualTo(professor.id());
        assertThat(alocacao.turmaId()).isEqualTo(turmaId);
        assertThat(alocacao.disciplinaId()).isEqualTo(disciplinaId);
        assertThat(professorAcademicoPort.listarAlocacoes(ESCOLA_PADRAO_ID, professor.id())).hasSize(1);
    }

    @Test
    void deveBloquearAlocacaoDuplicadaViaContratoInterno() {
        UUID funcionarioId = criarFuncionario("Professor Internal Duplicado", "professor.internal.duplicado@example.com");
        UUID periodoId = criarPeriodo("PROF-INT-2038.2", "2038-08-01", "2038-12-20");
        UUID turmaId = criarTurma("PROF-INT-B", "Professor Internal Turma B", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Professor Internal Ciências", 60);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 60);

        var professor = professorAcademicoPort.criarProfessor(
                ESCOLA_PADRAO_ID,
                new CriarProfessorSolicitacao(funcionarioId, null, null, true));

        var solicitacao = new AlocarProfessorTurmaDisciplinaSolicitacao(
                turmaDisciplinaId,
                java.time.LocalDate.parse("2038-08-01"),
                null,
                true);

        professorAcademicoPort.alocarProfessorTurmaDisciplina(ESCOLA_PADRAO_ID, professor.id(), solicitacao);

        assertThatThrownBy(() -> professorAcademicoPort.alocarProfessorTurmaDisciplina(
                ESCOLA_PADRAO_ID,
                professor.id(),
                solicitacao))
                .isInstanceOf(ProfessorTurmaDisciplinaDuplicadaException.class);
    }

    private UUID criarFuncionario(String nome, String email) {
        UUID pessoaId = UUID.randomUUID();
        UUID cargoId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES (?, ?, ?, ?, '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """, pessoaId, nome, cpfAleatorio(), email);

        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES (?, ?, ?)
                """, cargoId, "PROF-INTERNAL-" + System.nanoTime(), "Professor");

        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
                """, funcionarioId, pessoaId, cargoId);

        return funcionarioId;
    }

    private UUID criarPeriodo(String nome, String dataInicio, String dataFim) {
        UUID periodoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO periodo_letivo
                (id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo, id_escola, created_at)
                VALUES (?, ?, ?, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, periodoId, nome, Integer.parseInt(dataInicio.substring(0, 4)), dataInicio, dataFim);
        return periodoId;
    }

    private UUID criarTurma(String codigo, String nome, int capacidade, UUID periodoId) {
        UUID turmaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO turma
                (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, id_turno, ativo, id_escola, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, turmaId, codigo, nome, capacidade, periodoId, SERIE_PADRAO_ID, TURNO_MANHA_ID);
        return turmaId;
    }

    private UUID criarDisciplina(String nome, int cargaHoraria) {
        UUID disciplinaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO disciplina
                (id_disciplina, nome, carga_horaria, ativo, id_escola, created_at)
                VALUES (?, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, disciplinaId, nome, cargaHoraria);
        return disciplinaId;
    }

    private UUID vincularDisciplina(UUID turmaId, UUID disciplinaId, int cargaHoraria) {
        UUID turmaDisciplinaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO turma_disciplina
                (id_turma_disciplina, carga_horaria, created_at, id_disciplina, id_turma)
                VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?)
                """, turmaDisciplinaId, cargaHoraria, disciplinaId, turmaId);
        return turmaDisciplinaId;
    }

    private String cpfAleatorio() {
        return Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 11);
    }
}
