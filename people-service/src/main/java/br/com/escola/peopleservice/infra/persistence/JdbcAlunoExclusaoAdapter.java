package br.com.escola.peopleservice.infra.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.peopleservice.application.port.out.AlunoExclusaoPort;
import br.com.escola.peopleservice.infra.config.PersistenceProperties;

@Component
public class JdbcAlunoExclusaoAdapter implements AlunoExclusaoPort {

    private static final String TIPO_PESSOA_ALUNO = "ALUNO";
    private static final String MOTIVO_SAIDA_EXCLUSAO = "EXCLUSAO_SOLICITADA";

    private final PersistenceProperties properties;

    public JdbcAlunoExclusaoAdapter(PersistenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public void excluir(UUID alunoId, UUID escolaId) {
        validarConfiguracao();
        carregarDriver();
        try (Connection connection = DriverManager.getConnection(
                properties.url(), properties.username(), properties.password())) {
            connection.setAutoCommit(false);
            try {
                UUID pessoaId = buscarPessoaDoAlunoAtivo(connection, alunoId, escolaId);
                inativarAluno(connection, alunoId, escolaId);
                if (!pessoaPossuiOutroTipo(connection, pessoaId)) {
                    inativarPessoa(connection, pessoaId, escolaId);
                }
                connection.commit();
            } catch (RuntimeException | SQLException exception) {
                rollback(connection, exception);
                if (exception instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException("Falha ao excluir aluno no banco de pessoas", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao acessar banco de pessoas", exception);
        }
    }

    private UUID buscarPessoaDoAlunoAtivo(Connection connection, UUID alunoId, UUID escolaId)
            throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT a.id_pessoa
                FROM aluno a
                JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                WHERE a.id_aluno = ? AND a.id_escola = ? AND p.id_escola = ? AND a.ativo = TRUE
                """)) {
            set(statement, alunoId, escolaId, escolaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new RecursoNaoEncontradoException("Aluno nao encontrado");
                }
                return resultSet.getObject("id_pessoa", UUID.class);
            }
        }
    }

    private void inativarAluno(Connection connection, UUID alunoId, UUID escolaId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE aluno
                SET ativo = FALSE, data_saida = COALESCE(data_saida, ?),
                    motivo_saida = COALESCE(motivo_saida, ?), updated_at = ?
                WHERE id_aluno = ? AND id_escola = ? AND ativo = TRUE
                """)) {
            set(statement, LocalDate.now(), MOTIVO_SAIDA_EXCLUSAO, LocalDateTime.now(), alunoId, escolaId);
            if (statement.executeUpdate() != 1) {
                throw new RecursoNaoEncontradoException("Aluno nao encontrado");
            }
        }
    }

    private boolean pessoaPossuiOutroTipo(Connection connection, UUID pessoaId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT COUNT(1)
                FROM pessoa_tipo_pessoa ptp
                JOIN tipo_pessoa tp ON tp.id_tipo_pessoa = ptp.id_tipo_pessoa
                WHERE ptp.id_pessoa = ? AND UPPER(tp.codigo) <> ?
                """)) {
            set(statement, pessoaId, TIPO_PESSOA_ALUNO);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private void inativarPessoa(Connection connection, UUID pessoaId, UUID escolaId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE pessoa SET ativo = FALSE, updated_at = ?
                WHERE id_pessoa = ? AND id_escola = ?
                """)) {
            set(statement, LocalDateTime.now(), pessoaId, escolaId);
            if (statement.executeUpdate() != 1) {
                throw new RecursoNaoEncontradoException("Pessoa do aluno nao encontrada");
            }
        }
    }

    private void set(java.sql.PreparedStatement statement, Object... values) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            statement.setObject(index + 1, values[index]);
        }
    }

    private void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            original.addSuppressed(rollbackException);
        }
    }

    private void validarConfiguracao() {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("Banco de pessoas nao configurado");
        }
    }

    private void carregarDriver() {
        if (!StringUtils.hasText(properties.driverClassName())) {
            return;
        }
        try {
            Class.forName(properties.driverClassName());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Driver do banco de pessoas nao encontrado", exception);
        }
    }
}
