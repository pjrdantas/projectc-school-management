package br.com.escola.responsiblesservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.responsiblesservice.application.dto.CadastrarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.ExclusaoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.AtualizarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;
import br.com.escola.responsiblesservice.application.exception.ResponsavelDuplicadoException;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelWritePort;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesPersistenceProperties;

@Component
public class JdbcResponsavelWriteAdapter implements ResponsavelWritePort {

    private static final String INSERT_QUERY = """
            INSERT INTO responsavel (
                id_responsavel, nome_completo, cpf, email, telefone, rg,
                cep, logradouro, numero, complemento, bairro, cidade, uf,
                id_escola, escola_nome, ativo, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, TRUE, ?, NULL)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE responsavel
            SET nome_completo = ?, cpf = ?, email = ?, telefone = ?, rg = ?,
                cep = ?, logradouro = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, uf = ?,
                updated_at = ?
            WHERE id_responsavel = ?
              AND id_escola = ?
              AND ativo = TRUE
            """;

    private static final String FIND_UPDATED_QUERY = """
            SELECT id_responsavel, nome_completo, cpf, email, telefone, rg,
                   cep, logradouro, numero, complemento, bairro, cidade, uf,
                   id_escola, escola_nome, created_at
            FROM responsavel
            WHERE id_responsavel = ?
              AND id_escola = ?
              AND ativo = TRUE
            """;

    private static final String LOCK_ACTIVE_QUERY = """
            SELECT id_responsavel
            FROM responsavel
            WHERE id_responsavel = ?
              AND id_escola = ?
              AND ativo = TRUE
            FOR UPDATE
            """;

    private static final String HAS_STUDENT_LINK_QUERY = """
            SELECT 1
            FROM aluno_responsavel
            WHERE id_responsavel = ?
            FETCH FIRST 1 ROWS ONLY
            """;

    private static final String SOFT_DELETE_QUERY = """
            UPDATE responsavel
            SET ativo = FALSE,
                updated_at = ?
            WHERE id_responsavel = ?
              AND id_escola = ?
              AND ativo = TRUE
            """;

    private final ResponsiblesPersistenceProperties properties;

    public JdbcResponsavelWriteAdapter(ResponsiblesPersistenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public ResponsavelReadModelResponse criar(CadastrarResponsavelCommand command, UUID escolaId) {
        requireUrl();
        loadDriver();

        UUID responsavelId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        try (var connection = DriverManager.getConnection(properties.url(), properties.username(), properties.password())) {
            connection.setAutoCommit(false);
            try (var statement = connection.prepareStatement(INSERT_QUERY)) {
                statement.setObject(1, responsavelId);
                statement.setString(2, command.nomeCompleto());
                statement.setString(3, command.cpf());
                statement.setString(4, command.email());
                statement.setString(5, command.telefone());
                statement.setString(6, command.rg());
                statement.setString(7, command.cep());
                statement.setString(8, command.logradouro());
                statement.setString(9, command.numero());
                statement.setString(10, command.complemento());
                statement.setString(11, command.bairro());
                statement.setString(12, command.cidade());
                statement.setString(13, command.uf());
                statement.setObject(14, escolaId);
                statement.setTimestamp(15, Timestamp.valueOf(createdAt));
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                rollback(connection);
                if (isUniqueViolation(exception)) {
                    throw new ResponsavelDuplicadoException("CPF ja cadastrado para esta escola", exception);
                }
                throw new IllegalStateException("responsibles-local-write-create-failed", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-write-connection-failed", exception);
        }

        return new ResponsavelReadModelResponse(
                responsavelId,
                command.nomeCompleto(),
                command.cpf(),
                command.email(),
                command.telefone(),
                command.rg(),
                command.cep(),
                command.logradouro(),
                command.numero(),
                command.complemento(),
                command.bairro(),
                command.cidade(),
                command.uf(),
                escolaId,
                null,
                createdAt);
    }

    @Override
    public Optional<ResponsavelReadModelResponse> atualizar(
            UUID responsavelId,
            AtualizarResponsavelCommand command,
            UUID escolaId) {
        requireUrl();
        loadDriver();

        try (var connection = DriverManager.getConnection(properties.url(), properties.username(), properties.password())) {
            connection.setAutoCommit(false);
            try (var update = connection.prepareStatement(UPDATE_QUERY)) {
                bind(update, command);
                update.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
                update.setObject(14, responsavelId);
                update.setObject(15, escolaId);
                if (update.executeUpdate() == 0) {
                    rollback(connection);
                    return Optional.empty();
                }
            } catch (SQLException exception) {
                rollback(connection);
                throw updateFailure(exception);
            }

            try (var find = connection.prepareStatement(FIND_UPDATED_QUERY)) {
                find.setObject(1, responsavelId);
                find.setObject(2, escolaId);
                try (var resultSet = find.executeQuery()) {
                    if (!resultSet.next()) {
                        rollback(connection);
                        return Optional.empty();
                    }
                    ResponsavelReadModelResponse response = map(resultSet);
                    connection.commit();
                    return Optional.of(response);
                }
            } catch (SQLException exception) {
                rollback(connection);
                throw updateFailure(exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-write-connection-failed", exception);
        }
    }

    @Override
    public ExclusaoResponsavelResultado excluir(UUID responsavelId, UUID escolaId) {
        requireUrl();
        loadDriver();

        try (var connection = DriverManager.getConnection(properties.url(), properties.username(), properties.password())) {
            connection.setAutoCommit(false);
            if (!lockActiveResponsavel(connection, responsavelId, escolaId)) {
                rollback(connection);
                return ExclusaoResponsavelResultado.NAO_ENCONTRADO;
            }
            if (hasStudentLink(connection, responsavelId)) {
                rollback(connection);
                return ExclusaoResponsavelResultado.POSSUI_ALUNO_VINCULADO;
            }
            try (var update = connection.prepareStatement(SOFT_DELETE_QUERY)) {
                update.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                update.setObject(2, responsavelId);
                update.setObject(3, escolaId);
                if (update.executeUpdate() == 0) {
                    rollback(connection);
                    return ExclusaoResponsavelResultado.NAO_ENCONTRADO;
                }
                connection.commit();
                return ExclusaoResponsavelResultado.EXCLUIDO;
            } catch (SQLException exception) {
                rollback(connection);
                throw new IllegalStateException("responsibles-local-write-delete-failed", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-write-connection-failed", exception);
        }
    }

    private void requireUrl() {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("responsibles-local-write-url-required");
        }
    }

    private void loadDriver() {
        if (!StringUtils.hasText(properties.driverClassName())) {
            return;
        }
        try {
            Class.forName(properties.driverClassName());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("responsibles-local-write-driver-not-found", exception);
        }
    }

    private boolean isUniqueViolation(SQLException exception) {
        return "23505".equals(exception.getSQLState());
    }

    private void bind(java.sql.PreparedStatement statement, AtualizarResponsavelCommand command) throws SQLException {
        statement.setString(1, command.nomeCompleto());
        statement.setString(2, command.cpf());
        statement.setString(3, command.email());
        statement.setString(4, command.telefone());
        statement.setString(5, command.rg());
        statement.setString(6, command.cep());
        statement.setString(7, command.logradouro());
        statement.setString(8, command.numero());
        statement.setString(9, command.complemento());
        statement.setString(10, command.bairro());
        statement.setString(11, command.cidade());
        statement.setString(12, command.uf());
    }

    private ResponsavelReadModelResponse map(java.sql.ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new ResponsavelReadModelResponse(
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

    private RuntimeException updateFailure(SQLException exception) {
        if (isUniqueViolation(exception)) {
            return new ResponsavelDuplicadoException("CPF ja cadastrado para esta escola", exception);
        }
        return new IllegalStateException("responsibles-local-write-update-failed", exception);
    }

    private boolean lockActiveResponsavel(java.sql.Connection connection, UUID responsavelId, UUID escolaId)
            throws SQLException {
        try (var statement = connection.prepareStatement(LOCK_ACTIVE_QUERY)) {
            statement.setObject(1, responsavelId);
            statement.setObject(2, escolaId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean hasStudentLink(java.sql.Connection connection, UUID responsavelId) throws SQLException {
        try (var statement = connection.prepareStatement(HAS_STUDENT_LINK_QUERY)) {
            statement.setObject(1, responsavelId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void rollback(java.sql.Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            throw new IllegalStateException("responsibles-local-write-rollback-failed", rollbackException);
        }
    }
}
