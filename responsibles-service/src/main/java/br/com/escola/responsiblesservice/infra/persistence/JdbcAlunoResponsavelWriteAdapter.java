package br.com.escola.responsiblesservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.responsiblesservice.application.dto.CriacaoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.DesvinculoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.VincularAlunoResponsavelCommand;
import br.com.escola.responsiblesservice.application.port.out.AlunoResponsavelWritePort;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesPersistenceProperties;

@Component
public class JdbcAlunoResponsavelWriteAdapter implements AlunoResponsavelWritePort {

    private static final String LOCK_RESPONSAVEL_QUERY = """
            SELECT id_responsavel
            FROM responsavel
            WHERE id_responsavel = ?
              AND id_escola = ?
              AND ativo = TRUE
            FOR UPDATE
            """;

    private static final String FIND_PARENTESCO_QUERY = """
            SELECT id_parentesco
            FROM parentesco
            WHERE codigo = ?
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO aluno_responsavel (
                id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                responsavel_financeiro, responsavel_pedagogico, autorizado_retirar
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String DELETE_QUERY = """
            DELETE FROM aluno_responsavel
            WHERE id_aluno = ?
              AND id_responsavel = ?
            """;

    private final ResponsiblesPersistenceProperties properties;

    public JdbcAlunoResponsavelWriteAdapter(ResponsiblesPersistenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public CriacaoAlunoResponsavelResultado vincular(
            UUID alunoId,
            VincularAlunoResponsavelCommand command,
            UUID escolaId) {
        requireUrl();
        loadDriver();

        try (var connection = DriverManager.getConnection(properties.url(), properties.username(), properties.password())) {
            connection.setAutoCommit(false);
            if (!responsavelAtivo(connection, command.responsavelId(), escolaId)) {
                rollback(connection);
                return CriacaoAlunoResponsavelResultado.RESPONSAVEL_NAO_ENCONTRADO;
            }
            UUID parentescoId = parentescoId(connection, command.parentesco());
            if (parentescoId == null) {
                rollback(connection);
                return CriacaoAlunoResponsavelResultado.PARENTESCO_NAO_ENCONTRADO;
            }
            try (var statement = connection.prepareStatement(INSERT_QUERY)) {
                statement.setObject(1, UUID.randomUUID());
                statement.setObject(2, alunoId);
                statement.setObject(3, command.responsavelId());
                statement.setObject(4, parentescoId);
                statement.setBoolean(5, Boolean.TRUE.equals(command.responsavelFinanceiro()));
                statement.setBoolean(6, Boolean.TRUE.equals(command.responsavelPedagogico()));
                statement.setBoolean(7, Boolean.TRUE.equals(command.autorizadoRetirar()));
                statement.executeUpdate();
                connection.commit();
                return CriacaoAlunoResponsavelResultado.CRIADO;
            } catch (SQLException exception) {
                rollback(connection);
                if ("23505".equals(exception.getSQLState())) {
                    return CriacaoAlunoResponsavelResultado.VINCULO_DUPLICADO;
                }
                throw new IllegalStateException("responsibles-local-write-student-link-create-failed", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-write-connection-failed", exception);
        }
    }

    @Override
    public DesvinculoAlunoResponsavelResultado desvincular(UUID alunoId, UUID responsavelId, UUID escolaId) {
        requireUrl();
        loadDriver();

        try (var connection = DriverManager.getConnection(properties.url(), properties.username(), properties.password())) {
            connection.setAutoCommit(false);
            if (!responsavelAtivo(connection, responsavelId, escolaId)) {
                rollback(connection);
                return DesvinculoAlunoResponsavelResultado.VINCULO_NAO_ENCONTRADO;
            }
            try (var statement = connection.prepareStatement(DELETE_QUERY)) {
                statement.setObject(1, alunoId);
                statement.setObject(2, responsavelId);
                if (statement.executeUpdate() == 0) {
                    rollback(connection);
                    return DesvinculoAlunoResponsavelResultado.VINCULO_NAO_ENCONTRADO;
                }
                connection.commit();
                return DesvinculoAlunoResponsavelResultado.DESVINCULADO;
            } catch (SQLException exception) {
                rollback(connection);
                throw new IllegalStateException("responsibles-local-write-student-link-delete-failed", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-write-connection-failed", exception);
        }
    }

    private boolean responsavelAtivo(java.sql.Connection connection, UUID responsavelId, UUID escolaId) throws SQLException {
        try (var statement = connection.prepareStatement(LOCK_RESPONSAVEL_QUERY)) {
            statement.setObject(1, responsavelId);
            statement.setObject(2, escolaId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private UUID parentescoId(java.sql.Connection connection, String parentesco) throws SQLException {
        try (var statement = connection.prepareStatement(FIND_PARENTESCO_QUERY)) {
            statement.setString(1, parentesco);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getObject("id_parentesco", UUID.class) : null;
            }
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

    private void rollback(java.sql.Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            throw new IllegalStateException("responsibles-local-write-rollback-failed", exception);
        }
    }
}
