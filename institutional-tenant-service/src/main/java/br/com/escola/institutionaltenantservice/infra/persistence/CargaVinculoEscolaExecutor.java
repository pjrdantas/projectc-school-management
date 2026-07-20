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
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class CargaVinculoEscolaExecutor {

    private static final List<String> COLUMNS = List.of(
            "id_usuario_escola", "id_usuario", "id_escola", "created_at");

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final int batchSize;

    public CargaVinculoEscolaExecutor(JdbcTemplate source, JdbcTemplate target, int batchSize) {
        this.source = source;
        this.target = target;
        this.batchSize = batchSize;
    }

    public CargaVinculoEscolaReport execute() {
        validateSchools();
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        int copiedRows = Objects.requireNonNull(transaction.execute(status -> copyAll()));
        int sourceRows = count(source);
        int targetRows = count(target);
        boolean reconciled = sourceRows == targetRows && digest(source).equals(digest(target));
        return new CargaVinculoEscolaReport(copiedRows, sourceRows, targetRows, reconciled);
    }

    private void validateSchools() {
        List<UUID> schoolIds = source.queryForList(
                "SELECT DISTINCT id_escola FROM usuario_escola ORDER BY id_escola", UUID.class);
        for (UUID schoolId : schoolIds) {
            Integer existing = target.queryForObject(
                    "SELECT COUNT(1) FROM escola WHERE id_escola = ?", Integer.class, schoolId);
            if (existing == null || existing != 1) {
                throw new IllegalStateException("Escola do vinculo ausente no destino: " + schoolId);
            }
        }
    }

    private int copyAll() {
        int offset = 0;
        int copied = 0;
        List<Map<String, Object>> rows;
        do {
            rows = source.queryForList("SELECT " + String.join(", ", COLUMNS)
                    + " FROM usuario_escola ORDER BY id_usuario_escola LIMIT ? OFFSET ?", batchSize, offset);
            rows.forEach(this::upsert);
            copied += rows.size();
            offset += rows.size();
        } while (rows.size() == batchSize);
        return copied;
    }

    private void upsert(Map<String, Object> row) {
        int updated = target.update("""
                UPDATE usuario_escola
                SET id_usuario = ?, id_escola = ?, created_at = ?
                WHERE id_usuario_escola = ?
                """, row.get("id_usuario"), row.get("id_escola"), row.get("created_at"),
                row.get("id_usuario_escola"));
        if (updated == 0) {
            target.update("""
                    INSERT INTO usuario_escola (
                        id_usuario_escola, id_usuario, id_escola, created_at
                    ) VALUES (?, ?, ?, ?)
                    """, row.get("id_usuario_escola"), row.get("id_usuario"),
                    row.get("id_escola"), row.get("created_at"));
        }
    }

    private int count(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM usuario_escola", Integer.class);
    }

    private String digest(JdbcTemplate jdbcTemplate) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            jdbcTemplate.queryForList("SELECT " + String.join(", ", COLUMNS)
                    + " FROM usuario_escola ORDER BY id_usuario_escola")
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
