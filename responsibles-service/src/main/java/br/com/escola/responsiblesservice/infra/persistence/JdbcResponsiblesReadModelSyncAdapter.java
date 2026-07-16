package br.com.escola.responsiblesservice.infra.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.responsiblesservice.application.port.out.ResponsiblesReadModelSyncPort;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary.TableOperationReport;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelMigrationProperties;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelSourceProperties;

@Component
public class JdbcResponsiblesReadModelSyncAdapter implements ResponsiblesReadModelSyncPort {

    private static final String SOURCE_QUERY = """
            SELECT r.id_responsavel,
                   p.nome_completo,
                   p.cpf,
                   p.email,
                   p.telefone,
                   p.rg,
                   e.cep,
                   e.logradouro,
                   e.numero,
                   e.complemento,
                   e.bairro,
                   e.cidade,
                   e.uf,
                   esc.id_escola,
                   esc.nome AS escola_nome,
                   r.created_at
            FROM responsavel r
              JOIN pessoa p ON p.id_pessoa = r.id_pessoa
              JOIN escola esc ON esc.id_escola = p.id_escola
              LEFT JOIN pessoa_endereco pe
                ON pe.id_pessoa = p.id_pessoa
               AND pe.principal = TRUE
              LEFT JOIN endereco e ON e.id_endereco = pe.id_endereco
            ORDER BY p.nome_completo, r.id_responsavel
            LIMIT ?
            """;

    private static final String TARGET_QUERY = """
            SELECT id_responsavel, nome_completo, cpf, email, telefone, rg,
                   cep, logradouro, numero, complemento, bairro, cidade, uf,
                   id_escola, escola_nome, created_at
            FROM responsavel
            ORDER BY nome_completo, id_responsavel
            """;

    private static final String UPDATE_QUERY = """
            UPDATE responsavel
               SET nome_completo = ?, cpf = ?, email = ?, telefone = ?, rg = ?,
                   cep = ?, logradouro = ?, numero = ?, complemento = ?, bairro = ?,
                   cidade = ?, uf = ?, id_escola = ?, escola_nome = ?, created_at = ?
             WHERE id_responsavel = ?
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO responsavel (
                id_responsavel, nome_completo, cpf, email, telefone, rg,
                cep, logradouro, numero, complemento, bairro, cidade, uf,
                id_escola, escola_nome, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final ResponsiblesReadModelSourceProperties sourceProperties;
    private final ResponsiblesReadModelMigrationProperties targetProperties;

    public JdbcResponsiblesReadModelSyncAdapter(
            ResponsiblesReadModelSourceProperties sourceProperties,
            ResponsiblesReadModelMigrationProperties targetProperties) {
        this.sourceProperties = sourceProperties;
        this.targetProperties = targetProperties;
    }

    @Override
    public List<TableOperationReport> synchronize(boolean backfillEnabled, int batchSize) {
        if (!StringUtils.hasText(sourceProperties.sourceUrl()) || !StringUtils.hasText(targetProperties.url())) {
            return List.of(new TableOperationReport(
                    "responsavel",
                    "id_responsavel",
                    "monolith_jdbc",
                    "responsibles_read_model",
                    "blocked",
                    "responsibles-read-model-source-or-target-url-required",
                    backfillEnabled,
                    true,
                    0,
                    0,
                    0));
        }

        loadDriver(sourceProperties.sourceDriverClassName());
        loadDriver(targetProperties.driverClassName());

        try (var source = DriverManager.getConnection(
                sourceProperties.sourceUrl(),
                sourceProperties.sourceUsername(),
                sourceProperties.sourcePassword());
                var target = DriverManager.getConnection(
                        targetProperties.url(),
                        targetProperties.username(),
                        targetProperties.password())) {
            List<ResponsavelRow> sourceRows = readSourceRows(source, batchSize);
            int backfilledRecords = 0;
            if (backfillEnabled) {
                for (ResponsavelRow row : sourceRows) {
                    backfilledRecords += upsert(target, row);
                }
            }
            List<ResponsavelRow> targetRows = readTargetRows(target);
            return List.of(new TableOperationReport(
                    "responsavel",
                    "id_responsavel",
                    "monolith_jdbc",
                    "responsibles_read_model",
                    "success",
                    "responsibles-read-model-backfill-completed",
                    backfillEnabled,
                    true,
                    sourceRows.size(),
                    targetRows.size(),
                    backfilledRecords));
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-read-model-sync-failed", exception);
        }
    }

    private List<ResponsavelRow> readSourceRows(Connection source, int batchSize) throws SQLException {
        try (var statement = source.prepareStatement(SOURCE_QUERY)) {
            statement.setInt(1, Math.max(batchSize, 1));
            try (var resultSet = statement.executeQuery()) {
                List<ResponsavelRow> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(map(resultSet));
                }
                return rows;
            }
        }
    }

    private List<ResponsavelRow> readTargetRows(Connection target) throws SQLException {
        try (var statement = target.prepareStatement(TARGET_QUERY);
                var resultSet = statement.executeQuery()) {
            List<ResponsavelRow> rows = new ArrayList<>();
            while (resultSet.next()) {
                rows.add(map(resultSet));
            }
            return rows;
        }
    }

    private int upsert(Connection target, ResponsavelRow row) throws SQLException {
        try (var update = target.prepareStatement(UPDATE_QUERY)) {
            bindUpdate(update, row);
            int updated = update.executeUpdate();
            if (updated > 0) {
                return updated;
            }
        }
        try (var insert = target.prepareStatement(INSERT_QUERY)) {
            bindInsert(insert, row);
            return insert.executeUpdate();
        }
    }

    private void bindUpdate(java.sql.PreparedStatement statement, ResponsavelRow row) throws SQLException {
        statement.setString(1, row.nomeCompleto());
        statement.setString(2, row.cpf());
        statement.setString(3, row.email());
        statement.setString(4, row.telefone());
        statement.setString(5, row.rg());
        statement.setString(6, row.cep());
        statement.setString(7, row.logradouro());
        statement.setString(8, row.numero());
        statement.setString(9, row.complemento());
        statement.setString(10, row.bairro());
        statement.setString(11, row.cidade());
        statement.setString(12, row.uf());
        statement.setObject(13, row.escolaId());
        statement.setString(14, row.escolaNome());
        statement.setTimestamp(15, row.createdAt() == null ? null : Timestamp.valueOf(row.createdAt()));
        statement.setObject(16, row.id());
    }

    private void bindInsert(java.sql.PreparedStatement statement, ResponsavelRow row) throws SQLException {
        statement.setObject(1, row.id());
        statement.setString(2, row.nomeCompleto());
        statement.setString(3, row.cpf());
        statement.setString(4, row.email());
        statement.setString(5, row.telefone());
        statement.setString(6, row.rg());
        statement.setString(7, row.cep());
        statement.setString(8, row.logradouro());
        statement.setString(9, row.numero());
        statement.setString(10, row.complemento());
        statement.setString(11, row.bairro());
        statement.setString(12, row.cidade());
        statement.setString(13, row.uf());
        statement.setObject(14, row.escolaId());
        statement.setString(15, row.escolaNome());
        statement.setTimestamp(16, row.createdAt() == null ? null : Timestamp.valueOf(row.createdAt()));
    }

    private ResponsavelRow map(java.sql.ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new ResponsavelRow(
                resultSet.getObject("id_responsavel", UUID.class),
                resultSet.getString("nome_completo"),
                resultSet.getString("cpf"),
                resultSet.getString("email"),
                resultSet.getString("telefone"),
                resultSet.getString("rg"),
                resultSet.getString("cep"),
                resultSet.getString("logradouro"),
                resultSet.getString("numero"),
                resultSet.getString("complemento"),
                resultSet.getString("bairro"),
                resultSet.getString("cidade"),
                resultSet.getString("uf"),
                resultSet.getObject("id_escola", UUID.class),
                resultSet.getString("escola_nome"),
                createdAt == null ? null : createdAt.toLocalDateTime());
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("responsibles-read-model-sync-driver-not-found", exception);
        }
    }

    private record ResponsavelRow(
            UUID id,
            String nomeCompleto,
            String cpf,
            String email,
            String telefone,
            String rg,
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf,
            UUID escolaId,
            String escolaNome,
            java.time.LocalDateTime createdAt) {
    }
}
