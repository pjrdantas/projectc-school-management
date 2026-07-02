package br.com.escola.peopleservice.infra.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport.TableOperationReport;
import br.com.escola.peopleservice.application.port.out.PeopleCatalogReadModelSyncPort;
import br.com.escola.peopleservice.infra.config.PeopleCatalogReadModelBackfillProperties;
import br.com.escola.peopleservice.infra.config.PeopleLocalReadModelSchemaMigrationProperties;

@Component
public class JdbcPeopleCatalogReadModelSyncAdapter implements PeopleCatalogReadModelSyncPort {

    private static final List<CatalogTable> CATALOG_TABLES = List.of(
            new CatalogTable(
                    "tipo_pessoa",
                    "id_tipo_pessoa",
                    true,
                    "SELECT id_tipo_pessoa, codigo, descricao, created_at FROM tipo_pessoa ORDER BY codigo LIMIT ?",
                    "SELECT id_tipo_pessoa, codigo, descricao, created_at FROM tipo_pessoa ORDER BY codigo",
                    "UPDATE tipo_pessoa SET codigo = ?, descricao = ?, created_at = ? WHERE id_tipo_pessoa = ?",
                    "INSERT INTO tipo_pessoa (id_tipo_pessoa, codigo, descricao, created_at) VALUES (?, ?, ?, ?)"),
            new CatalogTable(
                    "tipo_endereco",
                    "id_tipo_endereco",
                    false,
                    "SELECT id_tipo_endereco, codigo, descricao FROM tipo_endereco ORDER BY codigo LIMIT ?",
                    "SELECT id_tipo_endereco, codigo, descricao FROM tipo_endereco ORDER BY codigo",
                    "UPDATE tipo_endereco SET codigo = ?, descricao = ? WHERE id_tipo_endereco = ?",
                    "INSERT INTO tipo_endereco (id_tipo_endereco, codigo, descricao) VALUES (?, ?, ?)"));

    private final PeopleCatalogReadModelBackfillProperties backfillProperties;
    private final PeopleLocalReadModelSchemaMigrationProperties targetProperties;

    public JdbcPeopleCatalogReadModelSyncAdapter(
            PeopleCatalogReadModelBackfillProperties backfillProperties,
            PeopleLocalReadModelSchemaMigrationProperties targetProperties) {
        this.backfillProperties = backfillProperties;
        this.targetProperties = targetProperties;
    }

    @Override
    public List<TableOperationReport> synchronize(boolean backfillEnabled, boolean reconciliationEnabled, int batchSize) {
        if (!StringUtils.hasText(backfillProperties.sourceUrl()) || !StringUtils.hasText(targetProperties.url())) {
            return CATALOG_TABLES.stream()
                    .map(table -> blockedReport(table, backfillEnabled, reconciliationEnabled))
                    .toList();
        }

        loadDriver(backfillProperties.sourceDriverClassName());
        loadDriver(targetProperties.driverClassName());

        try (Connection source = DriverManager.getConnection(
                backfillProperties.sourceUrl(),
                backfillProperties.sourceUsername(),
                backfillProperties.sourcePassword());
                Connection target = DriverManager.getConnection(
                        targetProperties.url(),
                        targetProperties.username(),
                        targetProperties.password())) {
            List<TableOperationReport> reports = new ArrayList<>();
            for (CatalogTable table : CATALOG_TABLES) {
                reports.add(synchronizeTable(source, target, table, backfillEnabled, reconciliationEnabled, batchSize));
            }
            return reports;
        } catch (SQLException ex) {
            throw new IllegalStateException("catalog-read-model-sync-failed", ex);
        }
    }

    private TableOperationReport synchronizeTable(
            Connection source,
            Connection target,
            CatalogTable table,
            boolean backfillEnabled,
            boolean reconciliationEnabled,
            int batchSize) throws SQLException {
        List<CatalogRow> sourceRows = readRows(source, table.sourceLimitedQuery(), table.hasCreatedAt(), batchSize);
        int backfilledRecords = 0;

        if (backfillEnabled) {
            for (CatalogRow row : sourceRows) {
                backfilledRecords += upsert(target, table, row);
            }
        }

        List<CatalogRow> targetRows = reconciliationEnabled
                ? readRows(target, table.targetQuery(), table.hasCreatedAt(), Integer.MAX_VALUE)
                : List.of();
        int divergences = reconciliationEnabled ? countDivergences(sourceRows, targetRows) : 0;
        String status = divergences == 0 ? "success" : "diverged";
        String reason = divergences == 0 ? "catalog-sync-completed" : "catalog-reconciliation-diverged";

        return new TableOperationReport(
                table.name(),
                table.keyColumn(),
                "monolith_jdbc",
                "people_read_model_catalog",
                status,
                reason,
                backfillEnabled,
                reconciliationEnabled,
                true,
                sourceRows.size(),
                reconciliationEnabled ? targetRows.size() : 0,
                backfilledRecords,
                divergences);
    }

    private List<CatalogRow> readRows(Connection connection, String query, boolean hasCreatedAt, int limit) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            if (query.contains("LIMIT ?")) {
                statement.setInt(1, Math.max(1, limit));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CatalogRow> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Timestamp createdAt = hasCreatedAt ? resultSet.getTimestamp("created_at") : null;
                    rows.add(new CatalogRow(
                            resultSet.getObject(1, UUID.class),
                            resultSet.getString("codigo"),
                            resultSet.getString("descricao"),
                            createdAt == null ? null : createdAt.toInstant()));
                }
                return rows;
            }
        }
    }

    private int upsert(Connection target, CatalogTable table, CatalogRow row) throws SQLException {
        try (PreparedStatement update = target.prepareStatement(table.updateSql())) {
            bindUpdate(update, table, row);
            int updated = update.executeUpdate();
            if (updated > 0) {
                return updated;
            }
        }

        try (PreparedStatement insert = target.prepareStatement(table.insertSql())) {
            bindInsert(insert, table, row);
            return insert.executeUpdate();
        }
    }

    private void bindUpdate(PreparedStatement statement, CatalogTable table, CatalogRow row) throws SQLException {
        statement.setString(1, row.codigo());
        statement.setString(2, row.descricao());
        if (table.hasCreatedAt()) {
            statement.setTimestamp(3, Timestamp.from(row.createdAt() == null ? Instant.now() : row.createdAt()));
            statement.setObject(4, row.id());
        } else {
            statement.setObject(3, row.id());
        }
    }

    private void bindInsert(PreparedStatement statement, CatalogTable table, CatalogRow row) throws SQLException {
        statement.setObject(1, row.id());
        statement.setString(2, row.codigo());
        statement.setString(3, row.descricao());
        if (table.hasCreatedAt()) {
            statement.setTimestamp(4, Timestamp.from(row.createdAt() == null ? Instant.now() : row.createdAt()));
        }
    }

    private int countDivergences(List<CatalogRow> sourceRows, List<CatalogRow> targetRows) {
        Map<String, CatalogRow> sourceByCode = byCode(sourceRows);
        Map<String, CatalogRow> targetByCode = byCode(targetRows);
        int divergences = 0;

        for (Map.Entry<String, CatalogRow> entry : sourceByCode.entrySet()) {
            CatalogRow target = targetByCode.get(entry.getKey());
            if (target == null) {
                divergences++;
                continue;
            }
            CatalogRow source = entry.getValue();
            if (!Objects.equals(source.id(), target.id()) || !Objects.equals(source.descricao(), target.descricao())) {
                divergences++;
            }
        }

        for (String targetCode : targetByCode.keySet()) {
            if (!sourceByCode.containsKey(targetCode)) {
                divergences++;
            }
        }

        return divergences;
    }

    private Map<String, CatalogRow> byCode(List<CatalogRow> rows) {
        Map<String, CatalogRow> byCode = new LinkedHashMap<>();
        for (CatalogRow row : rows) {
            byCode.put(row.codigo(), row);
        }
        return byCode;
    }

    private TableOperationReport blockedReport(
            CatalogTable table,
            boolean backfillEnabled,
            boolean reconciliationEnabled) {
        String reason = !StringUtils.hasText(backfillProperties.sourceUrl())
                ? "catalog-backfill-source-url-required"
                : "catalog-backfill-target-url-required";
        return new TableOperationReport(
                table.name(),
                table.keyColumn(),
                "monolith_jdbc",
                "people_read_model_catalog",
                "blocked",
                reason,
                backfillEnabled,
                reconciliationEnabled,
                true,
                0,
                0,
                0,
                0);
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("catalog-backfill-driver-not-found", ex);
        }
    }

    private record CatalogTable(
            String name,
            String keyColumn,
            boolean hasCreatedAt,
            String sourceLimitedQuery,
            String targetQuery,
            String updateSql,
            String insertSql) {
    }

    private record CatalogRow(UUID id, String codigo, String descricao, Instant createdAt) {
    }
}
