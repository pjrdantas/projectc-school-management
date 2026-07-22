package br.com.escola.pedagogicalservice.infra.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Imports legacy documentary history without inventing student or enrollment links absent from the source. */
public class HistoricoEscolarBackfillExecutor {

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final ObjectMapper objectMapper;
    private final UUID schoolId;
    private final int batchSize;

    public HistoricoEscolarBackfillExecutor(
            JdbcTemplate source, JdbcTemplate target, ObjectMapper objectMapper, UUID schoolId, int batchSize) {
        this.source = source;
        this.target = target;
        this.objectMapper = objectMapper;
        this.schoolId = schoolId;
        this.batchSize = batchSize;
    }

    public HistoricoEscolarBackfillReport execute() {
        List<HistoricoRow> rows = historicos();
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        transaction.executeWithoutResult(status -> rows.forEach(this::upsert));
        int reconciled = rows.stream().mapToInt(this::reconcile).sum();
        return new HistoricoEscolarBackfillReport(rows.size(), rows.size(), reconciled, rows.size() == reconciled);
    }

    private List<HistoricoRow> historicos() {
        List<HistoricoRow> rows = new ArrayList<>();
        int offset = 0;
        List<Map<String, Object>> batch;
        do {
            batch = source.queryForList("SELECT * FROM historico_escolar ORDER BY id LIMIT ? OFFSET ?", batchSize, offset);
            batch.forEach(row -> rows.add(new HistoricoRow(uuid(row, "id"), payload(row), itens(uuid(row, "id")))));
            offset += batch.size();
        } while (batch.size() == batchSize);
        return rows;
    }

    private List<Map<String, Object>> itens(UUID historicoId) {
        return source.queryForList("SELECT * FROM historico_escolar_item WHERE historico_escolar_id = ? ORDER BY id", historicoId);
    }

    private String payload(Map<String, Object> cabecalho) {
        ObjectNode root = objectMapper.createObjectNode();
        root.putObject("contexto")
                .put("modo", "HISTORICO_LEGADO")
                .put("status", "COMPLETO")
                .put("bloqueado", true);
        root.set("cabecalho", objectMapper.valueToTree(cabecalho));
        return json(root);
    }

    private String payloadCompleto(HistoricoRow row) {
        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(row.cabecalhoJson);
            ArrayNode itens = root.putArray("itensLegados");
            row.itens.forEach(item -> itens.add(objectMapper.valueToTree(item)));
            return json(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("pedagogical-history-backfill-payload-invalid", exception);
        }
    }

    private void upsert(HistoricoRow row) {
        String payload = payloadCompleto(row);
        int updated = target.update("""
                UPDATE academic_history_record SET school_id = ?, student_id = NULL, enrollment_id = NULL,
                    screen_mode = ?, screen_payload_json = ?, write_payload_json = ?, updated_at = ? WHERE id = ?
                """, schoolId, "HISTORICO_LEGADO", payload, "{\"origem\":\"backfill-legado\"}", LocalDateTime.now(), row.id);
        if (updated == 0) {
            target.update("""
                    INSERT INTO academic_history_record (
                        id, school_id, student_id, enrollment_id, screen_mode, screen_payload_json, write_payload_json, updated_at
                    ) VALUES (?, ?, NULL, NULL, ?, ?, ?, ?)
                    """, row.id, schoolId, "HISTORICO_LEGADO", payload, "{\"origem\":\"backfill-legado\"}", LocalDateTime.now());
        }
    }

    private int reconcile(HistoricoRow row) {
        String expectedPayload = payloadCompleto(row);
        return target.query("""
                SELECT school_id, student_id, enrollment_id, screen_mode, screen_payload_json
                FROM academic_history_record WHERE id = ?
                """, result -> result.next()
                        && Objects.equals(result.getObject("school_id", UUID.class), schoolId)
                        && result.getObject("student_id") == null
                        && result.getObject("enrollment_id") == null
                        && Objects.equals(result.getString("screen_mode"), "HISTORICO_LEGADO")
                        && Objects.equals(result.getString("screen_payload_json"), expectedPayload) ? 1 : 0, row.id);
    }

    private UUID uuid(Map<String, Object> row, String column) {
        return UUID.fromString(row.get(column).toString());
    }

    private String json(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("pedagogical-history-backfill-payload-invalid", exception);
        }
    }

    private record HistoricoRow(UUID id, String cabecalhoJson, List<Map<String, Object>> itens) {
    }
}
