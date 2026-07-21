package br.com.escola.responsiblesservice.infra.persistence;

import java.sql.Timestamp;
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

/** Copies the responsible aggregate without deleting records created after the cutover. */
public class ResponsiblesWriteBackfillExecutor {

    private static final String RESPONSAVEIS = "responsavel";
    private static final String PARENTESCOS = "parentesco";
    private static final String VINCULOS = "aluno_responsavel";

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final int batchSize;

    public ResponsiblesWriteBackfillExecutor(JdbcTemplate source, JdbcTemplate target, int batchSize) {
        this.source = source;
        this.target = target;
        this.batchSize = batchSize;
    }

    public ResponsiblesWriteBackfillReport execute() {
        List<ResponsavelRow> responsaveis = responsaveis();
        List<ParentescoRow> parentescos = parentescos();
        List<VinculoRow> vinculos = vinculos();
        Map<String, Integer> copiedRows = new LinkedHashMap<>();

        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        transaction.executeWithoutResult(status -> {
            parentescos.forEach(this::upsertParentesco);
            responsaveis.forEach(this::upsertResponsavel);
            vinculos.forEach(this::upsertVinculo);
            copiedRows.put(PARENTESCOS, parentescos.size());
            copiedRows.put(RESPONSAVEIS, responsaveis.size());
            copiedRows.put(VINCULOS, vinculos.size());
        });

        Map<String, Integer> sourceRows = Map.of(
                PARENTESCOS, parentescos.size(),
                RESPONSAVEIS, responsaveis.size(),
                VINCULOS, vinculos.size());
        Map<String, Integer> reconciledRows = Map.of(
                PARENTESCOS, parentescos.stream().mapToInt(this::reconcile).sum(),
                RESPONSAVEIS, responsaveis.stream().mapToInt(this::reconcile).sum(),
                VINCULOS, vinculos.stream().mapToInt(this::reconcile).sum());
        boolean reconciled = sourceRows.equals(reconciledRows);
        return new ResponsiblesWriteBackfillReport(
                Map.copyOf(copiedRows), sourceRows, reconciledRows, reconciled);
    }

    private List<ResponsavelRow> responsaveis() {
        return page("""
                SELECT r.id_responsavel, p.id_escola, e.nome AS escola_nome, p.nome_completo, p.cpf, p.email,
                       p.telefone, p.rg, endereco.cep, endereco.logradouro, endereco.numero,
                       endereco.complemento, endereco.bairro, endereco.cidade, endereco.uf,
                       p.ativo, r.created_at, p.updated_at
                FROM responsavel r
                JOIN pessoa p ON p.id_pessoa = r.id_pessoa
                JOIN escola e ON e.id_escola = p.id_escola
                LEFT JOIN pessoa_endereco pe ON pe.id_pessoa = p.id_pessoa AND pe.principal = TRUE
                LEFT JOIN endereco endereco ON endereco.id_endereco = pe.id_endereco
                ORDER BY r.id_responsavel
                """, row -> new ResponsavelRow(
                uuid(row, "id_responsavel"), uuid(row, "id_escola"), text(row, "escola_nome"),
                text(row, "nome_completo"), text(row, "cpf"), text(row, "email"), text(row, "telefone"),
                text(row, "rg"), text(row, "cep"), text(row, "logradouro"), text(row, "numero"),
                text(row, "complemento"), text(row, "bairro"), text(row, "cidade"), text(row, "uf"),
                bool(row, "ativo"), timestamp(row, "created_at"), timestamp(row, "updated_at")));
    }

    private List<ParentescoRow> parentescos() {
        return page("""
                SELECT p.id_parentesco, p.codigo, p.descricao
                FROM parentesco p
                WHERE p.codigo = 'RESPONSAVEL_LEGAL'
                   OR EXISTS (SELECT 1 FROM aluno_responsavel ar WHERE ar.id_parentesco = p.id_parentesco)
                ORDER BY p.codigo
                """, row -> new ParentescoRow(uuid(row, "id_parentesco"), text(row, "codigo"), text(row, "descricao")));
    }

    private List<VinculoRow> vinculos() {
        return page("""
                SELECT ar.id_aluno_responsavel, ar.id_aluno, ar.id_responsavel,
                       COALESCE(p.codigo, 'RESPONSAVEL_LEGAL') AS parentesco_codigo,
                       COALESCE(ar.responsavel_financeiro, FALSE) AS responsavel_financeiro,
                       COALESCE(ar.responsavel_pedagogico, FALSE) AS responsavel_pedagogico,
                       COALESCE(ar.autorizado_retirar, FALSE) AS autorizado_retirar, ar.created_at
                FROM aluno_responsavel ar
                JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
                LEFT JOIN parentesco p ON p.id_parentesco = ar.id_parentesco
                ORDER BY ar.id_aluno_responsavel
                """, row -> new VinculoRow(
                uuid(row, "id_aluno_responsavel"), uuid(row, "id_aluno"), uuid(row, "id_responsavel"),
                text(row, "parentesco_codigo"), bool(row, "responsavel_financeiro"),
                bool(row, "responsavel_pedagogico"), bool(row, "autorizado_retirar"), timestamp(row, "created_at")));
    }

    private <T> List<T> page(String query, RowMapper<T> mapper) {
        List<T> rows = new ArrayList<>();
        int offset = 0;
        List<Map<String, Object>> batch;
        do {
            batch = source.queryForList(query + " LIMIT ? OFFSET ?", batchSize, offset);
            for (Map<String, Object> row : batch) {
                rows.add(mapper.map(row));
            }
            offset += batch.size();
        } while (batch.size() == batchSize);
        return rows;
    }

    private void upsertParentesco(ParentescoRow row) {
        int updated = target.update("UPDATE parentesco SET descricao = ? WHERE codigo = ?", row.descricao, row.codigo);
        if (updated == 0) {
            target.update("INSERT INTO parentesco (id_parentesco, codigo, descricao) VALUES (?, ?, ?)",
                    row.id, row.codigo, row.descricao);
        }
    }

    private void upsertResponsavel(ResponsavelRow row) {
        int updated = target.update("""
                UPDATE responsavel SET id_escola = ?, escola_nome = ?, nome_completo = ?, cpf = ?, email = ?,
                    telefone = ?, rg = ?, cep = ?, logradouro = ?, numero = ?, complemento = ?, bairro = ?,
                    cidade = ?, uf = ?, ativo = ?, created_at = ?, updated_at = ?
                WHERE id_responsavel = ?
                """, row.escolaId, row.escolaNome, row.nomeCompleto, row.cpf, row.email, row.telefone, row.rg,
                row.cep, row.logradouro, row.numero, row.complemento, row.bairro, row.cidade, row.uf, row.ativo,
                row.createdAt, row.updatedAt, row.id);
        if (updated == 0) {
            target.update("""
                    INSERT INTO responsavel (
                        id_responsavel, id_escola, escola_nome, nome_completo, cpf, email, telefone, rg,
                        cep, logradouro, numero, complemento, bairro, cidade, uf, ativo, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, row.id, row.escolaId, row.escolaNome, row.nomeCompleto, row.cpf, row.email, row.telefone,
                    row.rg, row.cep, row.logradouro, row.numero, row.complemento, row.bairro, row.cidade, row.uf,
                    row.ativo, row.createdAt, row.updatedAt);
        }
    }

    private void upsertVinculo(VinculoRow row) {
        UUID parentescoId = target.queryForObject("SELECT id_parentesco FROM parentesco WHERE codigo = ?", UUID.class,
                row.parentescoCodigo);
        int updated = target.update("""
                UPDATE aluno_responsavel SET id_parentesco = ?, responsavel_financeiro = ?,
                    responsavel_pedagogico = ?, autorizado_retirar = ?, created_at = ?
                WHERE id_aluno = ? AND id_responsavel = ?
                """, parentescoId, row.responsavelFinanceiro, row.responsavelPedagogico, row.autorizadoRetirar,
                row.createdAt, row.alunoId, row.responsavelId);
        if (updated == 0) {
            target.update("""
                    INSERT INTO aluno_responsavel (
                        id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                        responsavel_financeiro, responsavel_pedagogico, autorizado_retirar, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, row.id, row.alunoId, row.responsavelId, parentescoId, row.responsavelFinanceiro,
                    row.responsavelPedagogico, row.autorizadoRetirar, row.createdAt);
        }
    }

    private int reconcile(ParentescoRow row) {
        return target.query("SELECT descricao FROM parentesco WHERE codigo = ?", result ->
                result.next() && Objects.equals(result.getString(1), row.descricao) ? 1 : 0, row.codigo);
    }

    private int reconcile(ResponsavelRow row) {
        return target.query("""
                SELECT id_escola, escola_nome, nome_completo, cpf, email, telefone, rg, cep, logradouro, numero,
                       complemento, bairro, cidade, uf, ativo, created_at, updated_at
                FROM responsavel WHERE id_responsavel = ?
                """, result -> {
                    if (!result.next()) {
                        return 0;
                    }
                    return Objects.equals(result.getObject("id_escola", UUID.class), row.escolaId)
                            && Objects.equals(result.getString("escola_nome"), row.escolaNome)
                            && Objects.equals(result.getString("nome_completo"), row.nomeCompleto)
                            && Objects.equals(result.getString("cpf"), row.cpf)
                            && Objects.equals(result.getBoolean("ativo"), row.ativo)
                            && Objects.equals(toLocalDateTime(result.getTimestamp("created_at")), row.createdAt)
                            && Objects.equals(toLocalDateTime(result.getTimestamp("updated_at")), row.updatedAt) ? 1 : 0;
                }, row.id);
    }

    private int reconcile(VinculoRow row) {
        return target.query("""
                SELECT p.codigo, ar.responsavel_financeiro, ar.responsavel_pedagogico, ar.autorizado_retirar, ar.created_at
                FROM aluno_responsavel ar JOIN parentesco p ON p.id_parentesco = ar.id_parentesco
                WHERE ar.id_aluno = ? AND ar.id_responsavel = ?
                """, result -> {
                    if (!result.next()) {
                        return 0;
                    }
                    return Objects.equals(result.getString("codigo"), row.parentescoCodigo)
                            && result.getBoolean("responsavel_financeiro") == row.responsavelFinanceiro
                            && result.getBoolean("responsavel_pedagogico") == row.responsavelPedagogico
                            && result.getBoolean("autorizado_retirar") == row.autorizadoRetirar
                            && Objects.equals(toLocalDateTime(result.getTimestamp("created_at")), row.createdAt) ? 1 : 0;
                }, row.alunoId, row.responsavelId);
    }

    private UUID uuid(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value instanceof UUID uuid ? uuid : UUID.fromString(value.toString());
    }

    private String text(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value == null ? null : value.toString();
    }

    private boolean bool(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value instanceof Boolean bool ? bool : Boolean.parseBoolean(value.toString());
    }

    private LocalDateTime timestamp(Map<String, Object> row, String column) {
        Object value = row.get(column);
        if (value == null) {
            return null;
        }
        return value instanceof Timestamp timestamp ? timestamp.toLocalDateTime() : (LocalDateTime) value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(Map<String, Object> row);
    }

    private record ResponsavelRow(
            UUID id, UUID escolaId, String escolaNome, String nomeCompleto, String cpf, String email, String telefone,
            String rg, String cep, String logradouro, String numero, String complemento, String bairro, String cidade,
            String uf, boolean ativo, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    private record ParentescoRow(UUID id, String codigo, String descricao) {
    }

    private record VinculoRow(
            UUID id, UUID alunoId, UUID responsavelId, String parentescoCodigo, boolean responsavelFinanceiro,
            boolean responsavelPedagogico, boolean autorizadoRetirar, LocalDateTime createdAt) {
    }
}
