package br.com.escola.institutionaltenantservice.infra.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class CargaEscolaExecutor {

    private static final List<String> COLUMNS = List.of(
            "id_escola", "nome", "codigo_inep", "cnpj", "telefone", "email",
            "id_endereco", "ativo", "created_at", "updated_at");

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final int batchSize;

    public CargaEscolaExecutor(JdbcTemplate source, JdbcTemplate target, int batchSize) {
        this.source = source;
        this.target = target;
        this.batchSize = batchSize;
    }

    public CargaEscolaReport execute() {
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        int copiedRows = Objects.requireNonNull(transaction.execute(status -> copyAll()));
        int sourceRows = count(source);
        int targetRows = count(target);
        boolean reconciled = sourceRows == targetRows && digest(source).equals(digest(target));
        return new CargaEscolaReport(copiedRows, sourceRows, targetRows, reconciled);
    }

    private int copyAll() {
        int offset = 0;
        int copied = 0;
        List<Map<String, Object>> rows;
        do {
            rows = source.queryForList("SELECT " + String.join(", ", COLUMNS)
                    + " FROM escola ORDER BY id_escola LIMIT ? OFFSET ?", batchSize, offset);
            rows.forEach(this::upsert);
            copied += rows.size();
            offset += rows.size();
        } while (rows.size() == batchSize);
        return copied;
    }

    private void upsert(Map<String, Object> row) {
        List<String> mutable = COLUMNS.stream().filter(column -> !column.equals("id_escola")).toList();
        Object[] updateArgs = new Object[mutable.size() + 1];
        for (int index = 0; index < mutable.size(); index++) {
            updateArgs[index] = row.get(mutable.get(index));
        }
        updateArgs[mutable.size()] = row.get("id_escola");
        int updated = target.update("UPDATE escola SET "
                + String.join(", ", mutable.stream().map(column -> column + " = ?").toList())
                + " WHERE id_escola = ?", updateArgs);
        if (updated == 0) {
            target.update("INSERT INTO escola (" + String.join(", ", COLUMNS) + ") VALUES ("
                    + String.join(", ", COLUMNS.stream().map(column -> "?").toList()) + ")",
                    COLUMNS.stream().map(row::get).toArray());
        }
    }

    private int count(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM escola", Integer.class);
    }

    private String digest(JdbcTemplate jdbcTemplate) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            jdbcTemplate.queryForList("SELECT " + String.join(", ", COLUMNS)
                    + " FROM escola ORDER BY id_escola")
                    .forEach(row -> COLUMNS.forEach(column -> {
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
}
