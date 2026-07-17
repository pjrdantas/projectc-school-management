package br.com.escola.responsiblesservice.infra.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.responsiblesservice.application.port.out.LeituraModeloSyncPort;
import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncSummary.TableOperationReport;
import br.com.escola.responsiblesservice.infra.config.LeituraModeloMigrationProperties;
import br.com.escola.responsiblesservice.infra.config.LeituraModeloSourceProperties;

@Component
public class JdbcLeituraModeloSyncAdapter implements LeituraModeloSyncPort {

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

    private static final String STUDENT_LINK_SOURCE_QUERY = """
            SELECT id_aluno_responsavel,
                   id_aluno,
                   id_responsavel,
                   id_parentesco,
                   responsavel_financeiro,
                   responsavel_pedagogico,
                   autorizado_retirar,
                   created_at
            FROM aluno_responsavel
            ORDER BY id_aluno, id_responsavel
            LIMIT ?
            """;

    private static final String STUDENT_LINK_TARGET_QUERY = """
            SELECT id_aluno_responsavel,
                   id_aluno,
                   id_responsavel,
                   id_parentesco,
                   responsavel_financeiro,
                   responsavel_pedagogico,
                   autorizado_retirar,
                   created_at
            FROM aluno_responsavel
            ORDER BY id_aluno, id_responsavel
            """;

    private static final String STUDENT_LINK_UPDATE_QUERY = """
            UPDATE aluno_responsavel
               SET id_aluno = ?, id_responsavel = ?, id_parentesco = ?, responsavel_financeiro = ?,
                   responsavel_pedagogico = ?, autorizado_retirar = ?, created_at = ?
             WHERE id_aluno_responsavel = ?
            """;

    private static final String STUDENT_LINK_INSERT_QUERY = """
            INSERT INTO aluno_responsavel (
                id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                responsavel_financeiro, responsavel_pedagogico, autorizado_retirar, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String KINSHIP_SOURCE_QUERY = """
            SELECT id_parentesco, codigo, descricao
            FROM parentesco
            ORDER BY codigo
            LIMIT ?
            """;

    private static final String KINSHIP_TARGET_QUERY = """
            SELECT id_parentesco, codigo, descricao
            FROM parentesco
            ORDER BY codigo
            """;

    private static final String KINSHIP_UPDATE_QUERY = """
            UPDATE parentesco
               SET codigo = ?, descricao = ?
             WHERE id_parentesco = ?
            """;

    private static final String KINSHIP_INSERT_QUERY = """
            INSERT INTO parentesco (id_parentesco, codigo, descricao)
            VALUES (?, ?, ?)
            """;

    private final LeituraModeloSourceProperties sourceProperties;
    private final LeituraModeloMigrationProperties targetProperties;

    public JdbcLeituraModeloSyncAdapter(
            LeituraModeloSourceProperties sourceProperties,
            LeituraModeloMigrationProperties targetProperties) {
        this.sourceProperties = sourceProperties;
        this.targetProperties = targetProperties;
    }

    @Override
    public List<TableOperationReport> synchronize(boolean backfillEnabled, boolean reconciliationEnabled, int batchSize) {
        if (!StringUtils.hasText(sourceProperties.sourceUrl()) || !StringUtils.hasText(targetProperties.url())) {
            return List.of(
                    blockedReport("responsavel", "id_responsavel", backfillEnabled, reconciliationEnabled),
                    blockedReport("parentesco", "id_parentesco", backfillEnabled, reconciliationEnabled),
                    blockedReport("aluno_responsavel", "id_aluno_responsavel", backfillEnabled, reconciliationEnabled));
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
            List<TableOperationReport> reports = new ArrayList<>();

            List<ResponsavelRow> sourceRows = readSourceRows(source, batchSize);
            int backfilledRecords = 0;
            if (backfillEnabled) {
                for (ResponsavelRow row : sourceRows) {
                    backfilledRecords += upsert(target, row);
                }
            }
            List<ResponsavelRow> targetRows = readTargetRows(target);
            reports.add(new TableOperationReport(
                    "responsavel",
                    "id_responsavel",
                    "monolith_jdbc",
                    "responsibles_read_model",
                    statusFor(reconciliationEnabled, sourceRows, targetRows),
                    reasonFor(reconciliationEnabled, sourceRows, targetRows),
                    backfillEnabled,
                    reconciliationEnabled,
                    true,
                    sourceRows.size(),
                    targetRows.size(),
                    backfilledRecords,
                    divergentRecords(sourceRows, targetRows)));

            List<ParentescoRow> sourceKinships = readKinshipSourceRows(source, batchSize);
            int backfilledKinships = 0;
            if (backfillEnabled) {
                for (ParentescoRow row : sourceKinships) {
                    backfilledKinships += upsertKinship(target, row);
                }
            }
            List<ParentescoRow> targetKinships = readKinshipTargetRows(target);
            reports.add(new TableOperationReport(
                    "parentesco",
                    "id_parentesco",
                    "monolith_jdbc",
                    "responsibles_read_model",
                    statusFor(reconciliationEnabled, sourceKinships, targetKinships),
                    reasonFor(reconciliationEnabled, sourceKinships, targetKinships),
                    backfillEnabled,
                    reconciliationEnabled,
                    true,
                    sourceKinships.size(),
                    targetKinships.size(),
                    backfilledKinships,
                    divergentRecords(sourceKinships, targetKinships)));

            List<AlunoResponsavelRow> sourceStudentLinks = readStudentLinkSourceRows(source, batchSize);
            int backfilledStudentLinks = 0;
            if (backfillEnabled) {
                for (AlunoResponsavelRow row : sourceStudentLinks) {
                    backfilledStudentLinks += upsertStudentLink(target, row);
                }
            }
            List<AlunoResponsavelRow> targetStudentLinks = readStudentLinkTargetRows(target);
            reports.add(new TableOperationReport(
                    "aluno_responsavel",
                    "id_aluno_responsavel",
                    "monolith_jdbc",
                    "responsibles_read_model",
                    statusFor(reconciliationEnabled, sourceStudentLinks, targetStudentLinks),
                    reasonFor(reconciliationEnabled, sourceStudentLinks, targetStudentLinks),
                    backfillEnabled,
                    reconciliationEnabled,
                    true,
                    sourceStudentLinks.size(),
                    targetStudentLinks.size(),
                    backfilledStudentLinks,
                    divergentRecords(sourceStudentLinks, targetStudentLinks)));

            return List.copyOf(reports);
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-read-model-sync-failed", exception);
        }
    }

    private TableOperationReport blockedReport(
            String table,
            String keyColumn,
            boolean backfillEnabled,
            boolean reconciliationEnabled) {
        return new TableOperationReport(
                table,
                keyColumn,
                "monolith_jdbc",
                "responsibles_read_model",
                "blocked",
                "responsibles-read-model-source-or-target-url-required",
                backfillEnabled,
                reconciliationEnabled,
                true,
                0,
                0,
                0,
                0);
    }

    private <T> String statusFor(boolean reconciliationEnabled, List<T> sourceRows, List<T> targetRows) {
        return reconciliationEnabled && divergentRecords(sourceRows, targetRows) > 0 ? "divergent" : "success";
    }

    private <T> String reasonFor(boolean reconciliationEnabled, List<T> sourceRows, List<T> targetRows) {
        return reconciliationEnabled && divergentRecords(sourceRows, targetRows) > 0
                ? "responsibles-read-model-reconciliation-divergent"
                : "responsibles-read-model-backfill-completed";
    }

    private <T> int divergentRecords(List<T> sourceRows, List<T> targetRows) {
        int divergent = Math.abs(sourceRows.size() - targetRows.size());
        int comparable = Math.min(sourceRows.size(), targetRows.size());
        for (int index = 0; index < comparable; index++) {
            if (!Objects.equals(sourceRows.get(index), targetRows.get(index))) {
                divergent++;
            }
        }
        return divergent;
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

    private List<AlunoResponsavelRow> readStudentLinkSourceRows(Connection source, int batchSize) throws SQLException {
        try (var statement = source.prepareStatement(STUDENT_LINK_SOURCE_QUERY)) {
            statement.setInt(1, Math.max(batchSize, 1));
            try (var resultSet = statement.executeQuery()) {
                List<AlunoResponsavelRow> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(mapStudentLink(resultSet));
                }
                return rows;
            }
        }
    }

    private List<AlunoResponsavelRow> readStudentLinkTargetRows(Connection target) throws SQLException {
        try (var statement = target.prepareStatement(STUDENT_LINK_TARGET_QUERY);
                var resultSet = statement.executeQuery()) {
            List<AlunoResponsavelRow> rows = new ArrayList<>();
            while (resultSet.next()) {
                rows.add(mapStudentLink(resultSet));
            }
            return rows;
        }
    }

    private List<ParentescoRow> readKinshipSourceRows(Connection source, int batchSize) throws SQLException {
        try (var statement = source.prepareStatement(KINSHIP_SOURCE_QUERY)) {
            statement.setInt(1, Math.max(batchSize, 1));
            try (var resultSet = statement.executeQuery()) {
                List<ParentescoRow> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(mapKinship(resultSet));
                }
                return rows;
            }
        }
    }

    private List<ParentescoRow> readKinshipTargetRows(Connection target) throws SQLException {
        try (var statement = target.prepareStatement(KINSHIP_TARGET_QUERY);
                var resultSet = statement.executeQuery()) {
            List<ParentescoRow> rows = new ArrayList<>();
            while (resultSet.next()) {
                rows.add(mapKinship(resultSet));
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

    private int upsertStudentLink(Connection target, AlunoResponsavelRow row) throws SQLException {
        try (var update = target.prepareStatement(STUDENT_LINK_UPDATE_QUERY)) {
            bindStudentLinkUpdate(update, row);
            int updated = update.executeUpdate();
            if (updated > 0) {
                return updated;
            }
        }
        try (var insert = target.prepareStatement(STUDENT_LINK_INSERT_QUERY)) {
            bindStudentLinkInsert(insert, row);
            return insert.executeUpdate();
        }
    }

    private int upsertKinship(Connection target, ParentescoRow row) throws SQLException {
        try (var update = target.prepareStatement(KINSHIP_UPDATE_QUERY)) {
            update.setString(1, row.codigo());
            update.setString(2, row.descricao());
            update.setObject(3, row.id());
            int updated = update.executeUpdate();
            if (updated > 0) {
                return updated;
            }
        }
        try (var insert = target.prepareStatement(KINSHIP_INSERT_QUERY)) {
            insert.setObject(1, row.id());
            insert.setString(2, row.codigo());
            insert.setString(3, row.descricao());
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

    private void bindStudentLinkUpdate(java.sql.PreparedStatement statement, AlunoResponsavelRow row) throws SQLException {
        statement.setObject(1, row.alunoId());
        statement.setObject(2, row.responsavelId());
        statement.setObject(3, row.parentescoId());
        statement.setBoolean(4, row.responsavelFinanceiro());
        statement.setBoolean(5, row.responsavelPedagogico());
        statement.setBoolean(6, row.autorizadoRetirar());
        statement.setTimestamp(7, row.createdAt() == null ? null : Timestamp.valueOf(row.createdAt()));
        statement.setObject(8, row.id());
    }

    private void bindStudentLinkInsert(java.sql.PreparedStatement statement, AlunoResponsavelRow row) throws SQLException {
        statement.setObject(1, row.id());
        statement.setObject(2, row.alunoId());
        statement.setObject(3, row.responsavelId());
        statement.setObject(4, row.parentescoId());
        statement.setBoolean(5, row.responsavelFinanceiro());
        statement.setBoolean(6, row.responsavelPedagogico());
        statement.setBoolean(7, row.autorizadoRetirar());
        statement.setTimestamp(8, row.createdAt() == null ? null : Timestamp.valueOf(row.createdAt()));
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

    private AlunoResponsavelRow mapStudentLink(java.sql.ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new AlunoResponsavelRow(
                resultSet.getObject("id_aluno_responsavel", UUID.class),
                resultSet.getObject("id_aluno", UUID.class),
                resultSet.getObject("id_responsavel", UUID.class),
                resultSet.getObject("id_parentesco", UUID.class),
                readBoolean(resultSet, "responsavel_financeiro"),
                readBoolean(resultSet, "responsavel_pedagogico"),
                readBoolean(resultSet, "autorizado_retirar"),
                createdAt == null ? null : createdAt.toLocalDateTime());
    }

    private ParentescoRow mapKinship(java.sql.ResultSet resultSet) throws SQLException {
        return new ParentescoRow(
                resultSet.getObject("id_parentesco", UUID.class),
                resultSet.getString("codigo"),
                resultSet.getString("descricao"));
    }

    private boolean readBoolean(java.sql.ResultSet resultSet, String column) throws SQLException {
        boolean value = resultSet.getBoolean(column);
        return resultSet.wasNull() ? false : value;
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

    private record AlunoResponsavelRow(
            UUID id,
            UUID alunoId,
            UUID responsavelId,
            UUID parentescoId,
            boolean responsavelFinanceiro,
            boolean responsavelPedagogico,
            boolean autorizadoRetirar,
            java.time.LocalDateTime createdAt) {

        private AlunoResponsavelRow {
            Objects.requireNonNull(id);
            Objects.requireNonNull(alunoId);
            Objects.requireNonNull(responsavelId);
        }
    }

    private record ParentescoRow(
            UUID id,
            String codigo,
            String descricao) {

        private ParentescoRow {
            Objects.requireNonNull(id);
            Objects.requireNonNull(codigo);
            Objects.requireNonNull(descricao);
        }
    }
}

