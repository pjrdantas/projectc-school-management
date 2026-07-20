package br.com.escola.identityaccessservice.infra.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class BackfillExecutor {

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final int batchSize;

    public BackfillExecutor(JdbcTemplate source, JdbcTemplate target, int batchSize) {
        this.source = source;
        this.target = target;
        this.batchSize = batchSize;
    }

    public BackfillReport execute() {
        Map<String, Integer> copied = new LinkedHashMap<>();
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        transaction.executeWithoutResult(status -> {
            for (TableSpec table : TableSpec.values()) {
                copied.put(table.table, copy(table));
            }
        });

        Map<String, Integer> sourceRows = counts(source);
        Map<String, Integer> targetRows = counts(target);
        boolean reconciled = sourceRows.equals(targetRows) && contentMatches();
        return new BackfillReport(
                Map.copyOf(copied),
                Map.copyOf(sourceRows),
                Map.copyOf(targetRows),
                reconciled);
    }

    private int copy(TableSpec table) {
        int offset = 0;
        int copied = 0;
        List<Map<String, Object>> rows;
        do {
            rows = source.queryForList("SELECT " + String.join(", ", table.columns)
                    + " FROM " + table.table
                    + " ORDER BY " + table.primaryKey
                    + " LIMIT ? OFFSET ?", batchSize, offset);
            rows.forEach(row -> upsert(table, row));
            copied += rows.size();
            offset += rows.size();
        } while (rows.size() == batchSize);
        return copied;
    }

    private void upsert(TableSpec table, Map<String, Object> row) {
        List<String> mutable = table.columns.stream()
                .filter(column -> !column.equals(table.primaryKey))
                .toList();
        Object[] updateArgs = new Object[mutable.size() + 1];
        for (int index = 0; index < mutable.size(); index++) {
            updateArgs[index] = row.get(mutable.get(index));
        }
        updateArgs[mutable.size()] = row.get(table.primaryKey);
        int updated = target.update("UPDATE " + table.table + " SET "
                + String.join(", ", mutable.stream().map(column -> column + " = ?").toList())
                + " WHERE " + table.primaryKey + " = ?", updateArgs);
        if (updated > 0) {
            return;
        }
        target.update("INSERT INTO " + table.table + " (" + String.join(", ", table.columns) + ") VALUES ("
                + String.join(", ", table.columns.stream().map(column -> "?").toList()) + ")",
                table.columns.stream().map(row::get).toArray());
    }

    private Map<String, Integer> counts(JdbcTemplate jdbcTemplate) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (TableSpec table : TableSpec.values()) {
            counts.put(table.table, jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM " + table.table, Integer.class));
        }
        return counts;
    }

    private boolean contentMatches() {
        for (TableSpec table : TableSpec.values()) {
            if (!digest(source, table).equals(digest(target, table))) {
                return false;
            }
        }
        return true;
    }

    private String digest(JdbcTemplate jdbcTemplate, TableSpec table) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            jdbcTemplate.queryForList("SELECT " + String.join(", ", table.columns)
                    + " FROM " + table.table + " ORDER BY " + table.primaryKey)
                    .forEach(row -> table.columns.forEach(column -> {
                        digest.update(normalize(row.get(column)).getBytes(StandardCharsets.UTF_8));
                        digest.update((byte) 0);
                    }));
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo de reconciliacao indisponivel", exception);
        }
    }

    private String normalize(Object value) {
        if (value == null) {
            return "<null>";
        }
        if (value instanceof Timestamp timestamp) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp.toLocalDateTime());
        }
        return value.toString();
    }

    private enum TableSpec {
        PERMISSAO("permissao", "id_permissao",
                "id_permissao", "codigo", "descricao", "created_at"),
        PERFIL("perfil", "id_perfil",
                "id_perfil", "codigo", "nome", "descricao", "created_at"),
        PERFIL_PERMISSAO("perfil_permissao", "id_perfil_permissao",
                "id_perfil_permissao", "id_perfil", "id_permissao"),
        USUARIO("usuario", "id_usuario",
                "id_usuario", "username", "nome", "email", "senha_hash", "ativo", "id_escola", "created_at"),
        USUARIO_PERFIL("usuario_perfil", "id_usuario_perfil",
                "id_usuario_perfil", "id_usuario", "id_perfil"),
        SESSAO("sessao_autenticacao", "id_sessao_autenticacao",
                "id_sessao_autenticacao", "id_usuario", "id_escola", "refresh_token_hash",
                "access_token_hash", "expira_em", "access_expira_em", "revogado", "created_at");

        private final String table;
        private final String primaryKey;
        private final List<String> columns;

        TableSpec(String table, String primaryKey, String... columns) {
            this.table = table;
            this.primaryKey = primaryKey;
            this.columns = List.of(columns);
        }
    }
}
