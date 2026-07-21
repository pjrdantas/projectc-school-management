package br.com.escola.enrollmentdocumentservice.infra.persistence;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Imports the enrollment aggregate without deleting records created in the new service. */
public class EnrollmentWriteBackfillExecutor {

    private static final String MATRICULAS = "matricula";
    private static final String ETAPAS = "matricula_etapa";

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final int batchSize;

    public EnrollmentWriteBackfillExecutor(JdbcTemplate source, JdbcTemplate target, int batchSize) {
        this.source = source;
        this.target = target;
        this.batchSize = batchSize;
    }

    public EnrollmentWriteBackfillReport execute() {
        List<MatriculaRow> matriculas = page("""
                SELECT m.id_matricula, p.id_escola, e.nome AS escola_nome, m.id_aluno, m.id_turma,
                       t.id_serie, s.nome AS serie_nome, m.id_periodo_letivo, sm.codigo AS status,
                       tm.codigo AS tipo_matricula, m.data_solicitacao, m.observacao, m.created_at
                FROM matricula m
                JOIN aluno a ON a.id_aluno = m.id_aluno
                JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                JOIN escola e ON e.id_escola = p.id_escola
                JOIN turma t ON t.id_turma = m.id_turma
                JOIN serie s ON s.id_serie = t.id_serie
                JOIN status_matricula sm ON sm.id_status_matricula = m.id_status_matricula
                JOIN tipo_matricula tm ON tm.id_tipo_matricula = m.id_tipo_matricula
                ORDER BY m.id_matricula
                """, row -> new MatriculaRow(
                uuid(row, "id_matricula"), uuid(row, "id_escola"), text(row, "escola_nome"),
                uuid(row, "id_aluno"), uuid(row, "id_turma"), uuid(row, "id_serie"), text(row, "serie_nome"),
                uuid(row, "id_periodo_letivo"), text(row, "status"), text(row, "tipo_matricula"),
                localDate(row, "data_solicitacao"), text(row, "observacao"), timestamp(row, "created_at")));
        List<EtapaRow> etapas = page("""
                SELECT me.id_matricula_etapa, me.id_matricula, me.descricao, me.ordem,
                       sem.codigo AS status, me.data_inicio, me.data_conclusao, me.observacao
                FROM matricula_etapa me
                JOIN status_etapa_matricula sem ON sem.id_status_etapa_matricula = me.id_status_etapa_matricula
                ORDER BY me.id_matricula_etapa
                """, row -> new EtapaRow(
                uuid(row, "id_matricula_etapa"), uuid(row, "id_matricula"), text(row, "descricao"),
                integer(row, "ordem"), text(row, "status"), timestamp(row, "data_inicio"),
                timestamp(row, "data_conclusao"), text(row, "observacao")));
        Map<String, Integer> copied = new LinkedHashMap<>();
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        transaction.executeWithoutResult(status -> {
            matriculas.forEach(this::upsertMatricula);
            etapas.forEach(this::upsertEtapa);
            copied.put(MATRICULAS, matriculas.size());
            copied.put(ETAPAS, etapas.size());
        });
        Map<String, Integer> sourceRows = Map.of(MATRICULAS, matriculas.size(), ETAPAS, etapas.size());
        Map<String, Integer> reconciledRows = Map.of(
                MATRICULAS, matriculas.stream().mapToInt(this::reconcile).sum(),
                ETAPAS, etapas.stream().mapToInt(this::reconcile).sum());
        return new EnrollmentWriteBackfillReport(
                Map.copyOf(copied), sourceRows, reconciledRows, sourceRows.equals(reconciledRows));
    }

    private void upsertMatricula(MatriculaRow row) {
        int updated = target.update("""
                UPDATE enrollment_record SET school_id = ?, student_id = ?, class_id = ?, school_name = ?, grade_id = ?,
                    grade_name = ?, term_id = ?, status = ?, enrollment_type = ?, enrollment_date = ?, note = ?,
                    created_at = ? WHERE id = ?
                """, row.escolaId, row.alunoId, row.turmaId, row.escolaNome, row.serieId, row.serieNome,
                row.periodoLetivoId, row.status, row.tipoMatricula, row.dataMatricula, row.observacao, row.createdAt, row.id);
        if (updated == 0) {
            target.update("""
                    INSERT INTO enrollment_record (
                        id, school_id, student_id, class_id, school_name, grade_id, grade_name, term_id,
                        status, enrollment_type, enrollment_date, note, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, row.id, row.escolaId, row.alunoId, row.turmaId, row.escolaNome, row.serieId, row.serieNome,
                    row.periodoLetivoId, row.status, row.tipoMatricula, row.dataMatricula, row.observacao, row.createdAt);
        }
    }

    private void upsertEtapa(EtapaRow row) {
        int updated = target.update("""
                UPDATE enrollment_step SET enrollment_id = ?, description = ?, step_order = ?, status = ?,
                    started_at = ?, completed_at = ?, note = ? WHERE id = ?
                """, row.matriculaId, row.descricao, row.ordem, row.status, row.dataInicio,
                row.dataConclusao, row.observacao, row.id);
        if (updated == 0) {
            target.update("""
                    INSERT INTO enrollment_step (
                        id, enrollment_id, description, step_order, status, started_at, completed_at, note
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, row.id, row.matriculaId, row.descricao, row.ordem, row.status, row.dataInicio,
                    row.dataConclusao, row.observacao);
        }
    }

    private int reconcile(MatriculaRow row) {
        return target.query("""
                SELECT school_id, student_id, class_id, grade_id, term_id, status, enrollment_type, enrollment_date
                FROM enrollment_record WHERE id = ?
                """, result -> result.next()
                && Objects.equals(result.getObject("school_id", UUID.class), row.escolaId)
                && Objects.equals(result.getObject("student_id", UUID.class), row.alunoId)
                && Objects.equals(result.getObject("class_id", UUID.class), row.turmaId)
                && Objects.equals(result.getObject("grade_id", UUID.class), row.serieId)
                && Objects.equals(result.getObject("term_id", UUID.class), row.periodoLetivoId)
                && Objects.equals(result.getString("status"), row.status)
                && Objects.equals(result.getString("enrollment_type"), row.tipoMatricula)
                && Objects.equals(result.getObject("enrollment_date", LocalDate.class), row.dataMatricula) ? 1 : 0, row.id);
    }

    private int reconcile(EtapaRow row) {
        return target.query("""
                SELECT enrollment_id, description, step_order, status FROM enrollment_step WHERE id = ?
                """, result -> result.next()
                && Objects.equals(result.getObject("enrollment_id", UUID.class), row.matriculaId)
                && Objects.equals(result.getString("description"), row.descricao)
                && Objects.equals(result.getObject("step_order", Integer.class), row.ordem)
                && Objects.equals(result.getString("status"), row.status) ? 1 : 0, row.id);
    }

    private <T> List<T> page(String query, Mapper<T> mapper) {
        List<T> result = new ArrayList<>();
        int offset = 0;
        List<Map<String, Object>> batch;
        do {
            batch = source.queryForList(query + " LIMIT ? OFFSET ?", batchSize, offset);
            batch.forEach(row -> result.add(mapper.map(row)));
            offset += batch.size();
        } while (batch.size() == batchSize);
        return result;
    }

    private UUID uuid(Map<String, Object> row, String column) { return (UUID) row.get(column); }
    private String text(Map<String, Object> row, String column) { return (String) row.get(column); }
    private int integer(Map<String, Object> row, String column) { return ((Number) row.get(column)).intValue(); }
    private LocalDate localDate(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value instanceof Date date ? date.toLocalDate() : (LocalDate) value;
    }
    private LocalDateTime timestamp(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value == null ? null : value instanceof Timestamp timestamp ? timestamp.toLocalDateTime() : (LocalDateTime) value;
    }

    @FunctionalInterface private interface Mapper<T> { T map(Map<String, Object> row); }
    private record MatriculaRow(UUID id, UUID escolaId, String escolaNome, UUID alunoId, UUID turmaId, UUID serieId,
            String serieNome, UUID periodoLetivoId, String status, String tipoMatricula, LocalDate dataMatricula,
            String observacao, LocalDateTime createdAt) { }
    private record EtapaRow(UUID id, UUID matriculaId, String descricao, int ordem, String status,
            LocalDateTime dataInicio, LocalDateTime dataConclusao, String observacao) { }
}
