package br.com.escola.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.avaliacao.adapter.out.persistence.entity.NotaAlunoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaDisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaDisciplinaJpaRepository;
import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardWidgetEntity;
import br.com.escola.dashboard.adapter.out.persistence.repository.DashboardWidgetJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.professor.adapter.out.persistence.entity.AulaEntity;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.AulaJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;

@SpringBootTest
@Transactional
class RelacionamentosJpaIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = uuid("00000000-0000-0000-0000-000000000100");
    private static final UUID TURNO_MANHA_ID = uuid("00000000-0000-0000-0000-000000000051");
    private static final UUID STATUS_ALUNO_ATIVO_ID = uuid("00000000-0000-0000-0000-000000000021");
    private static final UUID TIPO_MATRICULA_ID = uuid("00000000-0000-0000-0000-000000000061");
    private static final UUID STATUS_MATRICULA_ID = uuid("00000000-0000-0000-0000-000000000075");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TurmaDisciplinaJpaRepository turmaDisciplinaRepository;

    @Autowired
    private ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaRepository;

    @Autowired
    private AulaJpaRepository aulaRepository;

    @Autowired
    private NotaAlunoJpaRepository notaAlunoRepository;

    @Autowired
    private FrequenciaAlunoJpaRepository frequenciaAlunoRepository;

    @Autowired
    private DashboardWidgetJpaRepository dashboardWidgetRepository;

    @Test
    void devePersistirRelacionamentoEntreTurmaDisciplinaProfessorEAula() {
        UUID periodoId = uuid("10000000-0000-0000-0000-000000000001");
        UUID turmaId = uuid("10000000-0000-0000-0000-000000000002");
        UUID disciplinaId = uuid("10000000-0000-0000-0000-000000000003");
        UUID turmaDisciplinaId = uuid("10000000-0000-0000-0000-000000000004");
        UUID pessoaProfessorId = uuid("10000000-0000-0000-0000-000000000005");
        UUID professorId = uuid("10000000-0000-0000-0000-000000000006");
        UUID professorTurmaDisciplinaId = uuid("10000000-0000-0000-0000-000000000007");
        UUID aulaId = uuid("10000000-0000-0000-0000-000000000008");

        inserirBaseAcademica(periodoId, turmaId, disciplinaId, "REL-TURMA-A");
        inserirProfessor(pessoaProfessorId, professorId, "Professor Relacionamento A");
        inserirTurmaDisciplina(turmaDisciplinaId, turmaId, disciplinaId);
        inserirProfessorTurmaDisciplina(professorTurmaDisciplinaId, professorId, turmaDisciplinaId);
        inserirAula(aulaId, professorTurmaDisciplinaId);

        List<TurmaDisciplinaEntity> disciplinasDaTurma = turmaDisciplinaRepository.findByTurmaId(turmaId);
        assertThat(disciplinasDaTurma).hasSize(1);
        assertThat(disciplinasDaTurma.getFirst().getDisciplina().getId()).isEqualTo(disciplinaId);

        List<ProfessorTurmaDisciplinaEntity> vinculosDoProfessor =
                professorTurmaDisciplinaRepository.findByProfessorId(professorId);
        assertThat(vinculosDoProfessor).hasSize(1);
        assertThat(vinculosDoProfessor.getFirst().getTurmaDisciplina().getId()).isEqualTo(turmaDisciplinaId);

        List<AulaEntity> aulasDoVinculo = aulaRepository.findByProfessorTurmaDisciplinaId(professorTurmaDisciplinaId);
        assertThat(aulasDoVinculo).hasSize(1);
        assertThat(aulasDoVinculo.getFirst().getProfessorTurmaDisciplina().getProfessor().getId())
                .isEqualTo(professorId);
    }

    @Test
    void devePersistirNotaEFrequenciaPorMatricula() {
        UUID periodoId = uuid("20000000-0000-0000-0000-000000000001");
        UUID turmaId = uuid("20000000-0000-0000-0000-000000000002");
        UUID disciplinaId = uuid("20000000-0000-0000-0000-000000000003");
        UUID turmaDisciplinaId = uuid("20000000-0000-0000-0000-000000000004");
        UUID pessoaProfessorId = uuid("20000000-0000-0000-0000-000000000005");
        UUID professorId = uuid("20000000-0000-0000-0000-000000000006");
        UUID professorTurmaDisciplinaId = uuid("20000000-0000-0000-0000-000000000007");
        UUID aulaId = uuid("20000000-0000-0000-0000-000000000008");
        UUID pessoaAlunoId = uuid("20000000-0000-0000-0000-000000000009");
        UUID alunoId = uuid("20000000-0000-0000-0000-000000000010");
        UUID matriculaId = uuid("20000000-0000-0000-0000-000000000011");
        UUID tipoAvaliacaoId = uuid("20000000-0000-0000-0000-000000000012");
        UUID avaliacaoId = uuid("20000000-0000-0000-0000-000000000013");
        UUID notaId = uuid("20000000-0000-0000-0000-000000000014");
        UUID situacaoFrequenciaId = uuid("20000000-0000-0000-0000-000000000015");
        UUID frequenciaId = uuid("20000000-0000-0000-0000-000000000016");

        inserirBaseAcademica(periodoId, turmaId, disciplinaId, "REL-TURMA-B");
        inserirProfessor(pessoaProfessorId, professorId, "Professor Relacionamento B");
        inserirTurmaDisciplina(turmaDisciplinaId, turmaId, disciplinaId);
        inserirProfessorTurmaDisciplina(professorTurmaDisciplinaId, professorId, turmaDisciplinaId);
        inserirAula(aulaId, professorTurmaDisciplinaId);
        inserirAluno(pessoaAlunoId, alunoId, "Aluno Relacionamento B");
        inserirMatricula(matriculaId, alunoId, turmaId, periodoId);
        inserirAvaliacao(tipoAvaliacaoId, avaliacaoId, professorTurmaDisciplinaId);
        inserirNota(notaId, avaliacaoId, matriculaId);
        inserirFrequencia(situacaoFrequenciaId, frequenciaId, aulaId, matriculaId);

        List<NotaAlunoEntity> notas = notaAlunoRepository.findByMatriculaId(matriculaId);
        assertThat(notas).hasSize(1);
        assertThat(notas.getFirst().getAvaliacao().getProfessorTurmaDisciplina().getId())
                .isEqualTo(professorTurmaDisciplinaId);

        List<FrequenciaAlunoEntity> frequencias = frequenciaAlunoRepository.findByMatriculaId(matriculaId);
        assertThat(frequencias).hasSize(1);
        assertThat(frequencias.getFirst().getAula().getId()).isEqualTo(aulaId);
        assertThat(frequencias.getFirst().getSituacaoFrequencia().getCodigo()).isEqualTo("PRESENTE_TESTE");
    }

    @Test
    void devePersistirWidgetPorDashboard() {
        UUID publicoId = uuid("30000000-0000-0000-0000-000000000001");
        UUID dashboardId = uuid("30000000-0000-0000-0000-000000000002");
        UUID widgetId = uuid("30000000-0000-0000-0000-000000000003");

        jdbcTemplate.update("""
                INSERT INTO publico_dashboard (id_publico_dashboard, codigo, descricao)
                VALUES (?, 'GESTAO_TESTE', 'Gestao teste')
                """, publicoId);
        jdbcTemplate.update("""
                INSERT INTO dashboard (id_dashboard, id_publico_dashboard, codigo, nome, descricao, ativo, created_at)
                VALUES (?, ?, 'DASH_TESTE', 'Dashboard Teste', 'Dashboard de teste', true, CURRENT_TIMESTAMP)
                """, dashboardId, publicoId);
        jdbcTemplate.update("""
                INSERT INTO dashboard_widget (
                    id_dashboard_widget, id_dashboard, codigo, titulo, descricao,
                    tipo_widget, ordem, query_referencia, ativo, created_at
                )
                VALUES (?, ?, 'MATRICULAS_TESTE', 'Matriculas', 'Total de matriculas',
                    'CARD', 1, 'matriculas.total', true, CURRENT_TIMESTAMP)
                """, widgetId, dashboardId);

        List<DashboardWidgetEntity> widgets = dashboardWidgetRepository.findByDashboardId(dashboardId);
        assertThat(widgets).hasSize(1);
        assertThat(widgets.getFirst().getDashboard().getPublicoDashboard().getCodigo()).isEqualTo("GESTAO_TESTE");

        assertThat(dashboardWidgetRepository.findByDashboardIdAndCodigo(dashboardId, "MATRICULAS_TESTE"))
                .isPresent()
                .get()
                .extracting(DashboardWidgetEntity::getTitulo)
                .isEqualTo("Matriculas");
    }

    private void inserirBaseAcademica(UUID periodoId, UUID turmaId, UUID disciplinaId, String codigoTurma) {
        jdbcTemplate.update("""
                INSERT INTO periodo_letivo (
                    id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo, created_at
                )
                VALUES (?, ?, 2026, DATE '2026-02-01', DATE '2026-12-20', true, CURRENT_TIMESTAMP)
                """, periodoId, codigoTurma);
        jdbcTemplate.update("""
                INSERT INTO turma (
                    id_turma, codigo, nome, capacidade, id_periodo_letivo,
                    id_serie, id_turno, ativo, created_at
                )
                VALUES (?, ?, ?, 30, ?, ?, ?, true, CURRENT_TIMESTAMP)
                """, turmaId, codigoTurma, codigoTurma, periodoId, SERIE_PADRAO_ID, TURNO_MANHA_ID);
        jdbcTemplate.update("""
                INSERT INTO disciplina (id_disciplina, nome, carga_horaria, ativo, created_at)
                VALUES (?, ?, 80, true, CURRENT_TIMESTAMP)
                """, disciplinaId, "Disciplina " + codigoTurma);
    }

    private void inserirProfessor(UUID pessoaId, UUID professorId, String nome) {
        inserirPessoa(pessoaId, nome);
        jdbcTemplate.update("""
                INSERT INTO professor (
                    id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at
                )
                VALUES (?, ?, ?, 'Licenciatura', true, CURRENT_TIMESTAMP)
                """, professorId, pessoaId, "REG-" + professorId.toString().substring(0, 8));
    }

    private void inserirAluno(UUID pessoaId, UUID alunoId, String nome) {
        inserirPessoa(pessoaId, nome);
        jdbcTemplate.update("""
                INSERT INTO aluno (
                    id_aluno, id_pessoa, id_status_aluno, ra, rm, emancipado,
                    data_ingresso, ativo, created_at
                )
                VALUES (?, ?, ?, ?, ?, false, DATE '2026-02-01', true, CURRENT_TIMESTAMP)
                """, alunoId, pessoaId, STATUS_ALUNO_ATIVO_ID, "RA-" + alunoId.toString().substring(0, 8),
                "RM-" + alunoId.toString().substring(0, 8));
    }

    private void inserirPessoa(UUID pessoaId, String nome) {
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, ativo, created_at)
                VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
                """, pessoaId, nome, cpfFrom(pessoaId));
    }

    private void inserirTurmaDisciplina(UUID turmaDisciplinaId, UUID turmaId, UUID disciplinaId) {
        jdbcTemplate.update("""
                INSERT INTO turma_disciplina (
                    id_turma_disciplina, id_turma, id_disciplina, carga_horaria, created_at
                )
                VALUES (?, ?, ?, 80, CURRENT_TIMESTAMP)
                """, turmaDisciplinaId, turmaId, disciplinaId);
    }

    private void inserirProfessorTurmaDisciplina(
            UUID professorTurmaDisciplinaId,
            UUID professorId,
            UUID turmaDisciplinaId) {
        jdbcTemplate.update("""
                INSERT INTO professor_turma_disciplina (
                    id_professor_turma_disciplina, id_professor, id_turma_disciplina,
                    data_inicio, ativo, created_at
                )
                VALUES (?, ?, ?, DATE '2026-02-01', true, CURRENT_TIMESTAMP)
                """, professorTurmaDisciplinaId, professorId, turmaDisciplinaId);
    }

    private void inserirAula(UUID aulaId, UUID professorTurmaDisciplinaId) {
        jdbcTemplate.update("""
                INSERT INTO aula (
                    id_aula, id_professor_turma_disciplina, data_aula,
                    conteudo_ministrado, realizada, created_at
                )
                VALUES (?, ?, DATE '2026-03-10', 'Conteudo teste', true, CURRENT_TIMESTAMP)
                """, aulaId, professorTurmaDisciplinaId);
    }

    private void inserirMatricula(UUID matriculaId, UUID alunoId, UUID turmaId, UUID periodoId) {
        jdbcTemplate.update("""
                INSERT INTO matricula (
                    id_matricula, id_aluno, id_turma, id_periodo_letivo,
                    id_status_matricula, id_tipo_matricula, data_solicitacao, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, DATE '2026-02-01', CURRENT_TIMESTAMP)
                """, matriculaId, alunoId, turmaId, periodoId, STATUS_MATRICULA_ID, TIPO_MATRICULA_ID);
    }

    private void inserirAvaliacao(UUID tipoAvaliacaoId, UUID avaliacaoId, UUID professorTurmaDisciplinaId) {
        jdbcTemplate.update("""
                INSERT INTO tipo_avaliacao (id_tipo_avaliacao, codigo, descricao)
                VALUES (?, 'PROVA_TESTE', 'Prova teste')
                """, tipoAvaliacaoId);
        jdbcTemplate.update("""
                INSERT INTO avaliacao (
                    id_avaliacao, id_professor_turma_disciplina, id_tipo_avaliacao,
                    titulo, descricao, data_aplicacao, valor_maximo, peso, created_at
                )
                VALUES (?, ?, ?, 'Avaliacao teste', 'Descricao teste', DATE '2026-03-20', 10, 1, CURRENT_TIMESTAMP)
                """, avaliacaoId, professorTurmaDisciplinaId, tipoAvaliacaoId);
    }

    private void inserirNota(UUID notaId, UUID avaliacaoId, UUID matriculaId) {
        jdbcTemplate.update("""
                INSERT INTO nota_aluno (id_nota_aluno, id_avaliacao, id_matricula, nota, created_at)
                VALUES (?, ?, ?, 8.50, CURRENT_TIMESTAMP)
                """, notaId, avaliacaoId, matriculaId);
    }

    private void inserirFrequencia(
            UUID situacaoFrequenciaId,
            UUID frequenciaId,
            UUID aulaId,
            UUID matriculaId) {
        jdbcTemplate.update("""
                INSERT INTO situacao_frequencia (id_situacao_frequencia, codigo, descricao)
                VALUES (?, 'PRESENTE_TESTE', 'Presente teste')
                """, situacaoFrequenciaId);
        jdbcTemplate.update("""
                INSERT INTO frequencia_aluno (
                    id_frequencia_aluno, id_aula, id_matricula,
                    id_situacao_frequencia, justificativa, created_at
                )
                VALUES (?, ?, ?, ?, null, CURRENT_TIMESTAMP)
                """, frequenciaId, aulaId, matriculaId, situacaoFrequenciaId);
    }

    private static UUID uuid(String value) {
        return UUID.fromString(value);
    }

    private static String cpfFrom(UUID value) {
        String digits = value.toString().replace("-", "");
        return digits.substring(digits.length() - 11);
    }
}
