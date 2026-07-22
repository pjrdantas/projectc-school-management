package br.com.escola.professorservice.infra.persistence;

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

/** Imports the professor aggregate without deleting records created in the service after cutover. */
public class ProfessorBackfillExecutor {

    private static final String PROFESSORES = "professor";
    private static final String ALOCACOES = "professor_turma_disciplina";

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final int batchSize;

    public ProfessorBackfillExecutor(JdbcTemplate source, JdbcTemplate target, int batchSize) {
        this.source = source;
        this.target = target;
        this.batchSize = batchSize;
    }

    public ProfessorBackfillReport execute() {
        List<ProfessorRow> professores = professores();
        List<AlocacaoRow> alocacoes = alocacoes();
        Map<String, Integer> copiedRows = new LinkedHashMap<>();
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        transaction.executeWithoutResult(status -> {
            professores.forEach(this::upsertProfessor);
            alocacoes.forEach(this::upsertAlocacao);
            copiedRows.put(PROFESSORES, professores.size());
            copiedRows.put(ALOCACOES, alocacoes.size());
        });

        Map<String, Integer> sourceRows = Map.of(PROFESSORES, professores.size(), ALOCACOES, alocacoes.size());
        Map<String, Integer> reconciledRows = Map.of(
                PROFESSORES, professores.stream().mapToInt(this::reconcile).sum(),
                ALOCACOES, alocacoes.stream().mapToInt(this::reconcile).sum());
        return new ProfessorBackfillReport(Map.copyOf(copiedRows), sourceRows, reconciledRows,
                sourceRows.equals(reconciledRows));
    }

    private List<ProfessorRow> professores() {
        return page("""
                SELECT pr.id_professor, pe.id_pessoa, pe.nome_completo, pe.id_escola, e.nome AS escola_nome,
                       pr.registro_profissional, pr.formacao, pr.ativo, pr.created_at, pr.updated_at, pr.id_usuario
                FROM professor pr
                JOIN pessoa pe ON pe.id_pessoa = pr.id_pessoa
                JOIN escola e ON e.id_escola = pe.id_escola
                ORDER BY pr.id_professor
                """, row -> new ProfessorRow(
                uuid(row, "id_professor"), uuid(row, "id_pessoa"), text(row, "nome_completo"),
                uuid(row, "id_escola"), text(row, "escola_nome"), text(row, "registro_profissional"),
                text(row, "formacao"), bool(row, "ativo"), timestamp(row, "created_at"),
                timestamp(row, "updated_at"), nullableUuid(row, "id_usuario")));
    }

    private List<AlocacaoRow> alocacoes() {
        return page("""
                SELECT ptd.id_professor_turma_disciplina, ptd.id_professor, ptd.id_turma_disciplina,
                       td.id_turma, t.nome AS turma_nome, td.id_disciplina, d.nome AS disciplina_nome,
                       ptd.data_inicio, ptd.data_fim, ptd.ativo, ptd.created_at
                FROM professor_turma_disciplina ptd
                JOIN turma_disciplina td ON td.id_turma_disciplina = ptd.id_turma_disciplina
                JOIN turma t ON t.id_turma = td.id_turma
                JOIN disciplina d ON d.id_disciplina = td.id_disciplina
                ORDER BY ptd.id_professor_turma_disciplina
                """, row -> new AlocacaoRow(
                uuid(row, "id_professor_turma_disciplina"), uuid(row, "id_professor"),
                uuid(row, "id_turma_disciplina"), uuid(row, "id_turma"), text(row, "turma_nome"),
                uuid(row, "id_disciplina"), text(row, "disciplina_nome"), date(row, "data_inicio"),
                date(row, "data_fim"), bool(row, "ativo"), timestamp(row, "created_at")));
    }

    private <T> List<T> page(String query, RowMapper<T> mapper) {
        List<T> rows = new ArrayList<>();
        int offset = 0;
        List<Map<String, Object>> batch;
        do {
            batch = source.queryForList(query + " LIMIT ? OFFSET ?", batchSize, offset);
            batch.forEach(row -> rows.add(mapper.map(row)));
            offset += batch.size();
        } while (batch.size() == batchSize);
        return rows;
    }

    private void upsertProfessor(ProfessorRow row) {
        int updated = target.update("""
                UPDATE professor SET id_pessoa = ?, nome_completo = ?, id_escola = ?, escola_nome = ?,
                    registro_profissional = ?, formacao = ?, ativo = ?, created_at = ?, updated_at = ?, id_usuario = ?
                WHERE id_professor = ?
                """, row.pessoaId, row.nomeCompleto, row.escolaId, row.escolaNome, row.registroProfissional,
                row.formacao, row.ativo, row.createdAt, row.updatedAt, row.usuarioId, row.id);
        if (updated == 0) {
            target.update("""
                    INSERT INTO professor (
                        id_professor, id_pessoa, nome_completo, id_escola, escola_nome, registro_profissional,
                        formacao, ativo, created_at, updated_at, id_usuario
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, row.id, row.pessoaId, row.nomeCompleto, row.escolaId, row.escolaNome,
                    row.registroProfissional, row.formacao, row.ativo, row.createdAt, row.updatedAt, row.usuarioId);
        }
    }

    private void upsertAlocacao(AlocacaoRow row) {
        int updated = target.update("""
                UPDATE professor_turma_disciplina SET id_professor = ?, id_turma_disciplina = ?, id_turma = ?,
                    nome_turma = ?, id_disciplina = ?, nome_disciplina = ?, data_inicio = ?, data_fim = ?,
                    ativo = ?, ativo_chave = ?, created_at = ?
                WHERE id_professor_turma_disciplina = ?
                """, row.professorId, row.turmaDisciplinaId, row.turmaId, row.turmaNome, row.disciplinaId,
                row.disciplinaNome, row.dataInicio, row.dataFim, row.ativo, activeKey(row.ativo), row.createdAt, row.id);
        if (updated == 0) {
            target.update("""
                    INSERT INTO professor_turma_disciplina (
                        id_professor_turma_disciplina, id_professor, id_turma_disciplina, id_turma, nome_turma,
                        id_disciplina, nome_disciplina, data_inicio, data_fim, ativo, ativo_chave, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, row.id, row.professorId, row.turmaDisciplinaId, row.turmaId, row.turmaNome,
                    row.disciplinaId, row.disciplinaNome, row.dataInicio, row.dataFim, row.ativo,
                    activeKey(row.ativo), row.createdAt);
        }
    }

    private int reconcile(ProfessorRow row) {
        return target.query("""
                SELECT id_pessoa, nome_completo, id_escola, escola_nome, registro_profissional, formacao, ativo,
                       created_at, updated_at, id_usuario FROM professor WHERE id_professor = ?
                """, result -> result.next()
                        && Objects.equals(result.getObject("id_pessoa", UUID.class), row.pessoaId)
                        && Objects.equals(result.getString("nome_completo"), row.nomeCompleto)
                        && Objects.equals(result.getObject("id_escola", UUID.class), row.escolaId)
                        && Objects.equals(result.getString("escola_nome"), row.escolaNome)
                        && Objects.equals(result.getString("registro_profissional"), row.registroProfissional)
                        && Objects.equals(result.getString("formacao"), row.formacao)
                        && result.getBoolean("ativo") == row.ativo
                        && Objects.equals(localDateTime(result.getTimestamp("created_at")), row.createdAt)
                        && Objects.equals(localDateTime(result.getTimestamp("updated_at")), row.updatedAt)
                        && Objects.equals(result.getObject("id_usuario", UUID.class), row.usuarioId) ? 1 : 0, row.id);
    }

    private int reconcile(AlocacaoRow row) {
        return target.query("""
                SELECT id_professor, id_turma_disciplina, id_turma, nome_turma, id_disciplina, nome_disciplina,
                       data_inicio, data_fim, ativo, created_at
                FROM professor_turma_disciplina WHERE id_professor_turma_disciplina = ?
                """, result -> result.next()
                        && Objects.equals(result.getObject("id_professor", UUID.class), row.professorId)
                        && Objects.equals(result.getObject("id_turma_disciplina", UUID.class), row.turmaDisciplinaId)
                        && Objects.equals(result.getObject("id_turma", UUID.class), row.turmaId)
                        && Objects.equals(result.getString("nome_turma"), row.turmaNome)
                        && Objects.equals(result.getObject("id_disciplina", UUID.class), row.disciplinaId)
                        && Objects.equals(result.getString("nome_disciplina"), row.disciplinaNome)
                        && Objects.equals(toLocalDate(result.getDate("data_inicio")), row.dataInicio)
                        && Objects.equals(toLocalDate(result.getDate("data_fim")), row.dataFim)
                        && result.getBoolean("ativo") == row.ativo
                        && Objects.equals(localDateTime(result.getTimestamp("created_at")), row.createdAt) ? 1 : 0, row.id);
    }

    private UUID uuid(Map<String, Object> row, String column) {
        return UUID.fromString(row.get(column).toString());
    }

    private UUID nullableUuid(Map<String, Object> row, String column) {
        return row.get(column) == null ? null : uuid(row, column);
    }

    private String text(Map<String, Object> row, String column) {
        return row.get(column) == null ? null : row.get(column).toString();
    }

    private boolean bool(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value instanceof Boolean booleanValue ? booleanValue : Boolean.parseBoolean(value.toString());
    }

    private LocalDateTime timestamp(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value == null ? null : value instanceof Timestamp timestamp ? timestamp.toLocalDateTime() : (LocalDateTime) value;
    }

    private LocalDate date(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value == null ? null : value instanceof Date date ? date.toLocalDate() : (LocalDate) value;
    }

    private LocalDateTime localDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }

    private Boolean activeKey(boolean active) {
        return active ? Boolean.TRUE : null;
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(Map<String, Object> row);
    }

    private record ProfessorRow(
            UUID id, UUID pessoaId, String nomeCompleto, UUID escolaId, String escolaNome,
            String registroProfissional, String formacao, boolean ativo, LocalDateTime createdAt,
            LocalDateTime updatedAt, UUID usuarioId) {
    }

    private record AlocacaoRow(
            UUID id, UUID professorId, UUID turmaDisciplinaId, UUID turmaId, String turmaNome,
            UUID disciplinaId, String disciplinaNome, LocalDate dataInicio, LocalDate dataFim,
            boolean ativo, LocalDateTime createdAt) {
    }
}
