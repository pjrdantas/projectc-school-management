package br.com.escola.dashboardqueryservice.infra.persistence;

import java.math.BigDecimal;
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

/** Imports dashboard state only for the school explicitly selected by the operator. */
public class PainelBackfillExecutor {

    private static final LocalDateTime LEGACY_TIMESTAMP = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final List<String> TABLES = List.of(
            "painel_publico", "painel_configuracao", "painel_widget", "painel_usuario_preferencia",
            "painel_indicador_snapshot");

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final UUID escolaId;
    private final int batchSize;

    public PainelBackfillExecutor(JdbcTemplate source, JdbcTemplate target, UUID escolaId, int batchSize) {
        this.source = source;
        this.target = target;
        this.escolaId = escolaId;
        this.batchSize = batchSize;
    }

    public PainelBackfillReport execute() {
        List<PublicoRow> publicos = page("SELECT id_publico_dashboard, codigo, descricao FROM publico_dashboard ORDER BY id_publico_dashboard", this::publico);
        List<PainelRow> paineis = page("SELECT id_dashboard, id_publico_dashboard, codigo, nome, descricao, ativo, created_at FROM dashboard ORDER BY id_dashboard", this::painel);
        List<WidgetRow> widgets = page("SELECT id_dashboard_widget, id_dashboard, codigo, titulo, descricao, tipo_widget, ordem, query_referencia, ativo, created_at FROM dashboard_widget ORDER BY id_dashboard_widget", this::widget);
        List<PreferenciaRow> preferencias = page("SELECT id_dashboard_usuario_configuracao, id_usuario, id_dashboard_widget, visivel, ordem, configuracao_json, created_at, updated_at FROM dashboard_usuario_configuracao ORDER BY id_dashboard_usuario_configuracao", this::preferencia);
        List<SnapshotRow> snapshots = page("""
                SELECT s.id_dashboard_indicador_snapshot, s.id_publico_dashboard, s.codigo_indicador, s.descricao,
                       s.valor_numeric, s.valor_texto, s.referencia_data, s.created_at, e.nome AS escola_nome
                FROM dashboard_indicador_snapshot s
                JOIN escola e ON e.id_escola = s.id_escola
                WHERE s.id_escola = ? ORDER BY s.id_dashboard_indicador_snapshot
                """, this::snapshot, escolaId);
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        transaction.executeWithoutResult(status -> {
            publicos.forEach(this::upsert);
            paineis.forEach(this::upsert);
            widgets.forEach(this::upsert);
            preferencias.forEach(this::upsert);
            snapshots.forEach(this::upsert);
        });
        Map<String, Integer> sourceRows = counts(publicos, paineis, widgets, preferencias, snapshots);
        Map<String, Integer> reconciledRows = Map.of(
                TABLES.get(0), publicos.stream().mapToInt(this::reconcile).sum(),
                TABLES.get(1), paineis.stream().mapToInt(this::reconcile).sum(),
                TABLES.get(2), widgets.stream().mapToInt(this::reconcile).sum(),
                TABLES.get(3), preferencias.stream().mapToInt(this::reconcile).sum(),
                TABLES.get(4), snapshots.stream().mapToInt(this::reconcile).sum());
        return new PainelBackfillReport(sourceRows, reconciledRows, sourceRows.equals(reconciledRows));
    }

    private Map<String, Integer> counts(List<?> publicos, List<?> paineis, List<?> widgets, List<?> preferencias, List<?> snapshots) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put(TABLES.get(0), publicos.size()); counts.put(TABLES.get(1), paineis.size());
        counts.put(TABLES.get(2), widgets.size()); counts.put(TABLES.get(3), preferencias.size());
        counts.put(TABLES.get(4), snapshots.size());
        return Map.copyOf(counts);
    }

    private <T> List<T> page(String query, RowMapper<T> mapper, Object... parameters) {
        List<T> rows = new ArrayList<>();
        int offset = 0;
        List<Map<String, Object>> batch;
        do {
            String paged = query + " LIMIT ? OFFSET ?";
            Object[] args = java.util.stream.Stream.concat(java.util.Arrays.stream(parameters), java.util.stream.Stream.of(batchSize, offset)).toArray();
            batch = source.queryForList(paged, args);
            batch.forEach(row -> rows.add(mapper.map(row)));
            offset += batch.size();
        } while (batch.size() == batchSize);
        return rows;
    }

    private void upsert(PublicoRow row) { updateOrInsert("UPDATE painel_publico SET escola_id=?, codigo=?, descricao=?, created_at=?, updated_at=? WHERE id=?", new Object[]{escolaId,row.codigo,row.descricao,LEGACY_TIMESTAMP,LEGACY_TIMESTAMP,row.id}, "INSERT INTO painel_publico (id, escola_id, codigo, descricao, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)", new Object[]{row.id,escolaId,row.codigo,row.descricao,LEGACY_TIMESTAMP,LEGACY_TIMESTAMP}); }
    private void upsert(PainelRow row) { updateOrInsert("UPDATE painel_configuracao SET escola_id=?, publico_id=?, codigo=?, nome=?, descricao=?, ativo=?, created_at=?, updated_at=? WHERE id=?", new Object[]{escolaId,row.publicoId,row.codigo,row.nome,row.descricao,row.ativo,row.createdAt,row.createdAt,row.id}, "INSERT INTO painel_configuracao (id, escola_id, publico_id, codigo, nome, descricao, ativo, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{row.id,escolaId,row.publicoId,row.codigo,row.nome,row.descricao,row.ativo,row.createdAt,row.createdAt}); }
    private void upsert(WidgetRow row) { updateOrInsert("UPDATE painel_widget SET escola_id=?, painel_id=?, codigo=?, titulo=?, descricao=?, tipo_widget=?, ordem=?, query_referencia=?, ativo=?, created_at=?, updated_at=? WHERE id=?", new Object[]{escolaId,row.painelId,row.codigo,row.titulo,row.descricao,row.tipo,row.ordem,row.query,row.ativo,row.createdAt,row.createdAt,row.id}, "INSERT INTO painel_widget (id, escola_id, painel_id, codigo, titulo, descricao, tipo_widget, ordem, query_referencia, ativo, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{row.id,escolaId,row.painelId,row.codigo,row.titulo,row.descricao,row.tipo,row.ordem,row.query,row.ativo,row.createdAt,row.createdAt}); }
    private void upsert(PreferenciaRow row) { updateOrInsert("UPDATE painel_usuario_preferencia SET escola_id=?, usuario_id=?, widget_id=?, visivel=?, ordem=?, configuracao_json=?, created_at=?, updated_at=? WHERE id=?", new Object[]{escolaId,row.usuarioId,row.widgetId,row.visivel,row.ordem,row.json,row.createdAt,row.updatedAt,row.id}, "INSERT INTO painel_usuario_preferencia (id, escola_id, usuario_id, widget_id, visivel, ordem, configuracao_json, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{row.id,escolaId,row.usuarioId,row.widgetId,row.visivel,row.ordem,row.json,row.createdAt,row.updatedAt}); }
    private void upsert(SnapshotRow row) { updateOrInsert("UPDATE painel_indicador_snapshot SET escola_id=?, publico_id=?, codigo_indicador=?, descricao=?, valor_numeric=?, valor_texto=?, escola_nome=?, referencia_data=?, created_at=?, updated_at=? WHERE id=?", new Object[]{escolaId,row.publicoId,row.codigo,row.descricao,row.numeric,row.text,row.escolaNome,row.data,row.createdAt,row.createdAt,row.id}, "INSERT INTO painel_indicador_snapshot (id, escola_id, publico_id, codigo_indicador, descricao, valor_numeric, valor_texto, escola_nome, referencia_data, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{row.id,escolaId,row.publicoId,row.codigo,row.descricao,row.numeric,row.text,row.escolaNome,row.data,row.createdAt,row.createdAt}); }
    private void updateOrInsert(String update, Object[] updateArgs, String insert, Object[] insertArgs) { if (target.update(update, updateArgs) == 0) target.update(insert, insertArgs); }

    private int reconcile(PublicoRow row) { return exists("SELECT 1 FROM painel_publico WHERE id=? AND escola_id=? AND codigo=? AND descricao=?", row.id, escolaId, row.codigo, row.descricao); }
    private int reconcile(PainelRow row) { return exists("SELECT 1 FROM painel_configuracao WHERE id=? AND escola_id=? AND publico_id=? AND codigo=? AND nome=? AND descricao IS NOT DISTINCT FROM ? AND ativo=?", row.id, escolaId, row.publicoId, row.codigo, row.nome, row.descricao, row.ativo); }
    private int reconcile(WidgetRow row) { return exists("SELECT 1 FROM painel_widget WHERE id=? AND escola_id=? AND painel_id=? AND codigo=? AND titulo=? AND descricao IS NOT DISTINCT FROM ? AND tipo_widget=? AND ordem=? AND query_referencia IS NOT DISTINCT FROM ? AND ativo=?", row.id, escolaId, row.painelId, row.codigo, row.titulo, row.descricao, row.tipo, row.ordem, row.query, row.ativo); }
    private int reconcile(PreferenciaRow row) { return exists("SELECT 1 FROM painel_usuario_preferencia WHERE id=? AND escola_id=? AND usuario_id=? AND widget_id=? AND visivel=? AND ordem IS NOT DISTINCT FROM ? AND configuracao_json IS NOT DISTINCT FROM ?", row.id, escolaId, row.usuarioId, row.widgetId, row.visivel, row.ordem, row.json); }
    private int reconcile(SnapshotRow row) { return exists("SELECT 1 FROM painel_indicador_snapshot WHERE id=? AND escola_id=? AND publico_id=? AND codigo_indicador=? AND descricao=? AND valor_numeric IS NOT DISTINCT FROM ? AND valor_texto IS NOT DISTINCT FROM ? AND escola_nome=? AND referencia_data=?", row.id, escolaId, row.publicoId, row.codigo, row.descricao, row.numeric, row.text, row.escolaNome, row.data); }
    private int exists(String sql, Object... args) { return target.query(sql, rs -> rs.next() ? 1 : 0, args); }

    private PublicoRow publico(Map<String,Object> row) { return new PublicoRow(uuid(row,"id_publico_dashboard"), text(row,"codigo"), text(row,"descricao")); }
    private PainelRow painel(Map<String,Object> row) { return new PainelRow(uuid(row,"id_dashboard"),uuid(row,"id_publico_dashboard"),text(row,"codigo"),text(row,"nome"),text(row,"descricao"),bool(row,"ativo"),time(row,"created_at")); }
    private WidgetRow widget(Map<String,Object> row) { return new WidgetRow(uuid(row,"id_dashboard_widget"),uuid(row,"id_dashboard"),text(row,"codigo"),text(row,"titulo"),text(row,"descricao"),text(row,"tipo_widget"),integer(row,"ordem"),text(row,"query_referencia"),bool(row,"ativo"),time(row,"created_at")); }
    private PreferenciaRow preferencia(Map<String,Object> row) { return new PreferenciaRow(uuid(row,"id_dashboard_usuario_configuracao"),uuid(row,"id_usuario"),uuid(row,"id_dashboard_widget"),bool(row,"visivel"),integer(row,"ordem"),text(row,"configuracao_json"),time(row,"created_at"),time(row,"updated_at")); }
    private SnapshotRow snapshot(Map<String,Object> row) { return new SnapshotRow(uuid(row,"id_dashboard_indicador_snapshot"),uuid(row,"id_publico_dashboard"),text(row,"codigo_indicador"),text(row,"descricao"),(BigDecimal)row.get("valor_numeric"),text(row,"valor_texto"),text(row,"escola_nome"),date(row,"referencia_data"),time(row,"created_at")); }
    private UUID uuid(Map<String,Object> row,String c) { return UUID.fromString(row.get(c).toString()); }
    private String text(Map<String,Object> row,String c) { return row.get(c)==null?null:row.get(c).toString(); }
    private boolean bool(Map<String,Object> row,String c) { Object v=row.get(c); return v instanceof Boolean b?b:Boolean.parseBoolean(v.toString()); }
    private Integer integer(Map<String,Object> row,String c) { return row.get(c)==null?null:((Number)row.get(c)).intValue(); }
    private LocalDateTime time(Map<String,Object> row,String c) { Object v=row.get(c); return v==null?LEGACY_TIMESTAMP:v instanceof Timestamp t?t.toLocalDateTime():(LocalDateTime)v; }
    private LocalDate date(Map<String,Object> row,String c) { Object v=row.get(c); return v instanceof Date d?d.toLocalDate():(LocalDate)v; }
    @FunctionalInterface private interface RowMapper<T> { T map(Map<String,Object> row); }
    private record PublicoRow(UUID id,String codigo,String descricao) {}
    private record PainelRow(UUID id,UUID publicoId,String codigo,String nome,String descricao,boolean ativo,LocalDateTime createdAt) {}
    private record WidgetRow(UUID id,UUID painelId,String codigo,String titulo,String descricao,String tipo,Integer ordem,String query,boolean ativo,LocalDateTime createdAt) {}
    private record PreferenciaRow(UUID id,UUID usuarioId,UUID widgetId,boolean visivel,Integer ordem,String json,LocalDateTime createdAt,LocalDateTime updatedAt) {}
    private record SnapshotRow(UUID id,UUID publicoId,String codigo,String descricao,BigDecimal numeric,String text,String escolaNome,LocalDate data,LocalDateTime createdAt) {}
}
