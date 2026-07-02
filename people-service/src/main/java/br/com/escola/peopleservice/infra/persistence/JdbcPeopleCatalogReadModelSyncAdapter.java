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

    private static final PessoaTable PESSOA_TABLE = new PessoaTable(
            """
                    SELECT p.id_pessoa, p.id_escola, e.nome AS escola_nome, p.nome_completo, p.cpf, p.rg,
                           p.orgao_emissor_rg, p.uf_rg, p.email, p.telefone, p.data_nascimento, p.sexo,
                           p.nome_social, p.nacionalidade, p.naturalidade, p.ativo, p.created_at, p.updated_at
                    FROM pessoa p
                    JOIN escola e ON e.id_escola = p.id_escola
                    ORDER BY p.id_pessoa
                    LIMIT ?
                    """,
            """
                    SELECT id_pessoa, id_escola, escola_nome, nome_completo, cpf, rg, orgao_emissor_rg, uf_rg,
                           email, telefone, data_nascimento, sexo, nome_social, nacionalidade, naturalidade,
                           ativo, created_at, updated_at
                    FROM pessoa
                    ORDER BY id_pessoa
                    """,
            """
                    UPDATE pessoa
                    SET id_escola = ?, escola_nome = ?, nome_completo = ?, cpf = ?, rg = ?, orgao_emissor_rg = ?,
                        uf_rg = ?, email = ?, telefone = ?, data_nascimento = ?, sexo = ?, nome_social = ?,
                        nacionalidade = ?, naturalidade = ?, ativo = ?, created_at = ?, updated_at = ?
                    WHERE id_pessoa = ?
                    """,
            """
                    INSERT INTO pessoa (
                        id_pessoa, id_escola, escola_nome, nome_completo, cpf, rg, orgao_emissor_rg, uf_rg, email, telefone,
                        data_nascimento, sexo, nome_social, nacionalidade, naturalidade, ativo, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """);

    private static final PessoaTipoPessoaTable PESSOA_TIPO_PESSOA_TABLE = new PessoaTipoPessoaTable(
            """
                    SELECT id_pessoa_tipo_pessoa, id_pessoa, id_tipo_pessoa, created_at
                    FROM pessoa_tipo_pessoa
                    ORDER BY id_pessoa, id_tipo_pessoa
                    LIMIT ?
                    """,
            """
                    SELECT id_pessoa_tipo_pessoa, id_pessoa, id_tipo_pessoa, created_at
                    FROM pessoa_tipo_pessoa
                    ORDER BY id_pessoa, id_tipo_pessoa
                    """,
            """
                    UPDATE pessoa_tipo_pessoa
                    SET id_pessoa = ?, id_tipo_pessoa = ?, created_at = ?
                    WHERE id_pessoa_tipo_pessoa = ?
                    """,
            """
                    INSERT INTO pessoa_tipo_pessoa (id_pessoa_tipo_pessoa, id_pessoa, id_tipo_pessoa, created_at)
                    VALUES (?, ?, ?, ?)
                    """);

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
            return blockedReports(backfillEnabled, reconciliationEnabled);
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
            reports.add(synchronizePessoa(source, target, backfillEnabled, reconciliationEnabled, batchSize));
            reports.add(synchronizePessoaTipoPessoa(source, target, backfillEnabled, reconciliationEnabled, batchSize));
            return reports;
        } catch (SQLException ex) {
            throw new IllegalStateException("local-read-model-sync-failed", ex);
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

    private TableOperationReport synchronizePessoa(
            Connection source,
            Connection target,
            boolean backfillEnabled,
            boolean reconciliationEnabled,
            int batchSize) throws SQLException {
        List<PessoaRow> sourceRows = readPessoaRows(source, PESSOA_TABLE.sourceLimitedQuery(), batchSize);
        int backfilledRecords = 0;

        if (backfillEnabled) {
            for (PessoaRow row : sourceRows) {
                backfilledRecords += upsertPessoa(target, row);
            }
        }

        List<PessoaRow> targetRows = reconciliationEnabled
                ? readPessoaRows(target, PESSOA_TABLE.targetQuery(), Integer.MAX_VALUE)
                : List.of();
        int divergences = reconciliationEnabled ? countPessoaDivergences(sourceRows, targetRows) : 0;
        String status = divergences == 0 ? "success" : "diverged";
        String reason = divergences == 0 ? "identity-sync-completed" : "identity-reconciliation-diverged";

        return new TableOperationReport(
                "pessoa",
                "id_pessoa",
                "monolith_jdbc",
                "people_read_model_identity",
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

    private TableOperationReport synchronizePessoaTipoPessoa(
            Connection source,
            Connection target,
            boolean backfillEnabled,
            boolean reconciliationEnabled,
            int batchSize) throws SQLException {
        List<PessoaTipoPessoaRow> sourceRows =
                readPessoaTipoPessoaRows(source, PESSOA_TIPO_PESSOA_TABLE.sourceLimitedQuery(), batchSize);
        int backfilledRecords = 0;

        if (backfillEnabled) {
            for (PessoaTipoPessoaRow row : sourceRows) {
                backfilledRecords += upsertPessoaTipoPessoa(target, row);
            }
        }

        List<PessoaTipoPessoaRow> targetRows = reconciliationEnabled
                ? readPessoaTipoPessoaRows(target, PESSOA_TIPO_PESSOA_TABLE.targetQuery(), Integer.MAX_VALUE)
                : List.of();
        int divergences = reconciliationEnabled ? countPessoaTipoPessoaDivergences(sourceRows, targetRows) : 0;
        String status = divergences == 0 ? "success" : "diverged";
        String reason = divergences == 0 ? "identity-sync-completed" : "identity-reconciliation-diverged";

        return new TableOperationReport(
                "pessoa_tipo_pessoa",
                "id_pessoa_tipo_pessoa",
                "monolith_jdbc",
                "people_read_model_identity",
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

    private List<PessoaRow> readPessoaRows(Connection connection, String query, int limit) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            if (query.contains("LIMIT ?")) {
                statement.setInt(1, Math.max(1, limit));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<PessoaRow> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                    java.sql.Date dataNascimento = resultSet.getDate("data_nascimento");
                    rows.add(new PessoaRow(
                            resultSet.getObject("id_pessoa", UUID.class),
                            resultSet.getObject("id_escola", UUID.class),
                            resultSet.getString("escola_nome"),
                            resultSet.getString("nome_completo"),
                            resultSet.getString("cpf"),
                            resultSet.getString("rg"),
                            resultSet.getString("orgao_emissor_rg"),
                            resultSet.getString("uf_rg"),
                            resultSet.getString("email"),
                            resultSet.getString("telefone"),
                            dataNascimento == null ? null : dataNascimento.toLocalDate(),
                            resultSet.getString("sexo"),
                            resultSet.getString("nome_social"),
                            resultSet.getString("nacionalidade"),
                            resultSet.getString("naturalidade"),
                            resultSet.getBoolean("ativo"),
                            createdAt == null ? null : createdAt.toInstant(),
                            updatedAt == null ? null : updatedAt.toInstant()));
                }
                return rows;
            }
        }
    }

    private List<PessoaTipoPessoaRow> readPessoaTipoPessoaRows(Connection connection, String query, int limit)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            if (query.contains("LIMIT ?")) {
                statement.setInt(1, Math.max(1, limit));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<PessoaTipoPessoaRow> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    rows.add(new PessoaTipoPessoaRow(
                            resultSet.getObject("id_pessoa_tipo_pessoa", UUID.class),
                            resultSet.getObject("id_pessoa", UUID.class),
                            resultSet.getObject("id_tipo_pessoa", UUID.class),
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

    private int upsertPessoa(Connection target, PessoaRow row) throws SQLException {
        try (PreparedStatement update = target.prepareStatement(PESSOA_TABLE.updateSql())) {
            bindPessoaUpdate(update, row);
            int updated = update.executeUpdate();
            if (updated > 0) {
                return updated;
            }
        }

        try (PreparedStatement insert = target.prepareStatement(PESSOA_TABLE.insertSql())) {
            bindPessoaInsert(insert, row);
            return insert.executeUpdate();
        }
    }

    private int upsertPessoaTipoPessoa(Connection target, PessoaTipoPessoaRow row) throws SQLException {
        try (PreparedStatement update = target.prepareStatement(PESSOA_TIPO_PESSOA_TABLE.updateSql())) {
            update.setObject(1, row.pessoaId());
            update.setObject(2, row.tipoPessoaId());
            update.setTimestamp(3, Timestamp.from(row.createdAt() == null ? Instant.now() : row.createdAt()));
            update.setObject(4, row.id());
            int updated = update.executeUpdate();
            if (updated > 0) {
                return updated;
            }
        }

        try (PreparedStatement insert = target.prepareStatement(PESSOA_TIPO_PESSOA_TABLE.insertSql())) {
            insert.setObject(1, row.id());
            insert.setObject(2, row.pessoaId());
            insert.setObject(3, row.tipoPessoaId());
            insert.setTimestamp(4, Timestamp.from(row.createdAt() == null ? Instant.now() : row.createdAt()));
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

    private void bindPessoaUpdate(PreparedStatement statement, PessoaRow row) throws SQLException {
        statement.setObject(1, row.escolaId());
        statement.setString(2, row.escolaNome());
        statement.setString(3, row.nomeCompleto());
        statement.setString(4, row.cpf());
        statement.setString(5, row.rg());
        statement.setString(6, row.orgaoEmissorRg());
        statement.setString(7, row.ufRg());
        statement.setString(8, row.email());
        statement.setString(9, row.telefone());
        statement.setDate(10, row.dataNascimento() == null ? null : java.sql.Date.valueOf(row.dataNascimento()));
        statement.setString(11, row.sexo());
        statement.setString(12, row.nomeSocial());
        statement.setString(13, row.nacionalidade());
        statement.setString(14, row.naturalidade());
        statement.setBoolean(15, row.ativo());
        statement.setTimestamp(16, Timestamp.from(row.createdAt() == null ? Instant.now() : row.createdAt()));
        statement.setTimestamp(17, row.updatedAt() == null ? null : Timestamp.from(row.updatedAt()));
        statement.setObject(18, row.id());
    }

    private void bindPessoaInsert(PreparedStatement statement, PessoaRow row) throws SQLException {
        statement.setObject(1, row.id());
        statement.setObject(2, row.escolaId());
        statement.setString(3, row.escolaNome());
        statement.setString(4, row.nomeCompleto());
        statement.setString(5, row.cpf());
        statement.setString(6, row.rg());
        statement.setString(7, row.orgaoEmissorRg());
        statement.setString(8, row.ufRg());
        statement.setString(9, row.email());
        statement.setString(10, row.telefone());
        statement.setDate(11, row.dataNascimento() == null ? null : java.sql.Date.valueOf(row.dataNascimento()));
        statement.setString(12, row.sexo());
        statement.setString(13, row.nomeSocial());
        statement.setString(14, row.nacionalidade());
        statement.setString(15, row.naturalidade());
        statement.setBoolean(16, row.ativo());
        statement.setTimestamp(17, Timestamp.from(row.createdAt() == null ? Instant.now() : row.createdAt()));
        statement.setTimestamp(18, row.updatedAt() == null ? null : Timestamp.from(row.updatedAt()));
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

    private int countPessoaDivergences(List<PessoaRow> sourceRows, List<PessoaRow> targetRows) {
        Map<UUID, PessoaRow> sourceById = byPessoaId(sourceRows);
        Map<UUID, PessoaRow> targetById = byPessoaId(targetRows);
        int divergences = 0;

        for (Map.Entry<UUID, PessoaRow> entry : sourceById.entrySet()) {
            PessoaRow target = targetById.get(entry.getKey());
            if (target == null || !entry.getValue().matches(target)) {
                divergences++;
            }
        }
        for (UUID targetId : targetById.keySet()) {
            if (!sourceById.containsKey(targetId)) {
                divergences++;
            }
        }
        return divergences;
    }

    private int countPessoaTipoPessoaDivergences(
            List<PessoaTipoPessoaRow> sourceRows,
            List<PessoaTipoPessoaRow> targetRows) {
        Map<UUID, PessoaTipoPessoaRow> sourceById = byPessoaTipoPessoaId(sourceRows);
        Map<UUID, PessoaTipoPessoaRow> targetById = byPessoaTipoPessoaId(targetRows);
        int divergences = 0;

        for (Map.Entry<UUID, PessoaTipoPessoaRow> entry : sourceById.entrySet()) {
            PessoaTipoPessoaRow target = targetById.get(entry.getKey());
            if (target == null || !entry.getValue().matches(target)) {
                divergences++;
            }
        }
        for (UUID targetId : targetById.keySet()) {
            if (!sourceById.containsKey(targetId)) {
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

    private Map<UUID, PessoaRow> byPessoaId(List<PessoaRow> rows) {
        Map<UUID, PessoaRow> byId = new LinkedHashMap<>();
        for (PessoaRow row : rows) {
            byId.put(row.id(), row);
        }
        return byId;
    }

    private Map<UUID, PessoaTipoPessoaRow> byPessoaTipoPessoaId(List<PessoaTipoPessoaRow> rows) {
        Map<UUID, PessoaTipoPessoaRow> byId = new LinkedHashMap<>();
        for (PessoaTipoPessoaRow row : rows) {
            byId.put(row.id(), row);
        }
        return byId;
    }

    private List<TableOperationReport> blockedReports(boolean backfillEnabled, boolean reconciliationEnabled) {
        List<TableOperationReport> reports = new ArrayList<>();
        CATALOG_TABLES.stream()
                .map(table -> blockedReport(table, backfillEnabled, reconciliationEnabled))
                .forEach(reports::add);
        reports.add(blockedReport("pessoa", "id_pessoa", backfillEnabled, reconciliationEnabled));
        reports.add(blockedReport(
                "pessoa_tipo_pessoa",
                "id_pessoa_tipo_pessoa",
                backfillEnabled,
                reconciliationEnabled));
        return reports;
    }

    private TableOperationReport blockedReport(
            CatalogTable table,
            boolean backfillEnabled,
            boolean reconciliationEnabled) {
        String reason = !StringUtils.hasText(backfillProperties.sourceUrl())
                ? "local-read-model-source-url-required"
                : "local-read-model-target-url-required";
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

    private TableOperationReport blockedReport(
            String table,
            String keyColumn,
            boolean backfillEnabled,
            boolean reconciliationEnabled) {
        String reason = !StringUtils.hasText(backfillProperties.sourceUrl())
                ? "local-read-model-source-url-required"
                : "local-read-model-target-url-required";
        return new TableOperationReport(
                table,
                keyColumn,
                "monolith_jdbc",
                "people_read_model_identity",
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
            throw new IllegalStateException("local-read-model-driver-not-found", ex);
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

    private record PessoaTable(
            String sourceLimitedQuery,
            String targetQuery,
            String updateSql,
            String insertSql) {
    }

    private record PessoaTipoPessoaTable(
            String sourceLimitedQuery,
            String targetQuery,
            String updateSql,
            String insertSql) {
    }

    private record PessoaRow(
            UUID id,
            UUID escolaId,
            String escolaNome,
            String nomeCompleto,
            String cpf,
            String rg,
            String orgaoEmissorRg,
            String ufRg,
            String email,
            String telefone,
            java.time.LocalDate dataNascimento,
            String sexo,
            String nomeSocial,
            String nacionalidade,
            String naturalidade,
            boolean ativo,
            Instant createdAt,
            Instant updatedAt) {

        boolean matches(PessoaRow other) {
            return Objects.equals(id, other.id)
                    && Objects.equals(escolaId, other.escolaId)
                    && Objects.equals(escolaNome, other.escolaNome)
                    && Objects.equals(nomeCompleto, other.nomeCompleto)
                    && Objects.equals(cpf, other.cpf)
                    && Objects.equals(rg, other.rg)
                    && Objects.equals(orgaoEmissorRg, other.orgaoEmissorRg)
                    && Objects.equals(ufRg, other.ufRg)
                    && Objects.equals(email, other.email)
                    && Objects.equals(telefone, other.telefone)
                    && Objects.equals(dataNascimento, other.dataNascimento)
                    && Objects.equals(sexo, other.sexo)
                    && Objects.equals(nomeSocial, other.nomeSocial)
                    && Objects.equals(nacionalidade, other.nacionalidade)
                    && Objects.equals(naturalidade, other.naturalidade)
                    && ativo == other.ativo;
        }
    }

    private record PessoaTipoPessoaRow(UUID id, UUID pessoaId, UUID tipoPessoaId, Instant createdAt) {

        boolean matches(PessoaTipoPessoaRow other) {
            return Objects.equals(id, other.id)
                    && Objects.equals(pessoaId, other.pessoaId)
                    && Objects.equals(tipoPessoaId, other.tipoPessoaId);
        }
    }
}
