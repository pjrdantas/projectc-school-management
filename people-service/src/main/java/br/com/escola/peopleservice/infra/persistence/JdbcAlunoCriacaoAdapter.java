package br.com.escola.peopleservice.infra.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.exception.ConflitoPessoaException;
import br.com.escola.peopleservice.application.model.AlunoCriado;
import br.com.escola.peopleservice.application.model.AlunoNovo;
import br.com.escola.peopleservice.application.port.out.AlunoCriacaoPort;
import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;

@Component
public class JdbcAlunoCriacaoAdapter implements AlunoCriacaoPort {

    private static final String TIPO_PESSOA_ALUNO = "ALUNO";
    private static final String TIPO_ENDERECO_RESIDENCIAL = "RESIDENCIAL";

    private final LeituraModeloMigrationProperties properties;

    public JdbcAlunoCriacaoAdapter(LeituraModeloMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public AlunoCriado criar(AlunoNovo aluno) {
        validarConfiguracao();
        carregarDriver();
        try (Connection connection = DriverManager.getConnection(
                properties.url(), properties.username(), properties.password())) {
            connection.setAutoCommit(false);
            try {
                AlunoCriado criado = persistir(connection, aluno);
                connection.commit();
                return criado;
            } catch (RuntimeException | SQLException exception) {
                rollback(connection, exception);
                if (exception instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (exception instanceof SQLException sqlException
                        && "23505".equals(sqlException.getSQLState())) {
                    throw new ConflitoPessoaException("Aluno ja cadastrado com o CPF informado");
                }
                throw new IllegalStateException("Falha ao criar aluno no banco de pessoas", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao acessar banco de pessoas", exception);
        }
    }

    private AlunoCriado persistir(Connection connection, AlunoNovo aluno) throws SQLException {
        if (cpfExiste(connection, aluno.cpf(), aluno.escolaId())) {
            throw new ConflitoPessoaException("Aluno ja cadastrado com o CPF informado");
        }

        UUID statusId = catalogoId(connection, "status_aluno", "id_status_aluno", aluno.statusAluno());
        UUID tipoPessoaId = catalogoId(connection, "tipo_pessoa", "id_tipo_pessoa", TIPO_PESSOA_ALUNO);
        UUID tipoEnderecoId = enderecoInformado(aluno)
                ? catalogoId(connection, "tipo_endereco", "id_tipo_endereco", TIPO_ENDERECO_RESIDENCIAL)
                : null;

        UUID pessoaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        inserirPessoa(connection, pessoaId, aluno, now);
        inserirTipoPessoa(connection, pessoaId, tipoPessoaId, now);
        if (tipoEnderecoId != null) {
            inserirEndereco(connection, pessoaId, tipoEnderecoId, aluno, now);
        }
        inserirAluno(connection, alunoId, pessoaId, statusId, aluno, now);
        return toCreated(alunoId, aluno, now);
    }

    private boolean cpfExiste(Connection connection, String cpf, UUID escolaId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT COUNT(1)
                FROM pessoa
                WHERE id_escola = ? AND cpf = ?
                """)) {
            statement.setObject(1, escolaId);
            statement.setString(2, cpf);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private UUID catalogoId(
            Connection connection,
            String table,
            String idColumn,
            String code) throws SQLException {
        String sql = "SELECT " + idColumn + " FROM " + table + " WHERE UPPER(codigo) = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Catalogo de aluno nao cadastrado: " + code);
                }
                return resultSet.getObject(idColumn, UUID.class);
            }
        }
    }

    private void inserirPessoa(
            Connection connection,
            UUID pessoaId,
            AlunoNovo aluno,
            LocalDateTime now) throws SQLException {
        try (var statement = connection.prepareStatement("""
                INSERT INTO pessoa (
                    id_pessoa, id_escola, escola_nome, nome_completo, cpf, rg,
                    orgao_emissor_rg, uf_rg, email, telefone, data_nascimento,
                    sexo, nome_social, nacionalidade, naturalidade, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            set(statement, pessoaId, aluno.escolaId(), aluno.escolaNome(), aluno.nomeCompleto(),
                    aluno.cpf(), aluno.rg(), aluno.orgaoEmissorRg(), aluno.ufRg(), aluno.email(),
                    aluno.telefone(), aluno.dataNascimento(), aluno.sexo(), aluno.nomeSocial(),
                    aluno.nacionalidade(), aluno.naturalidade(), true, now);
            statement.executeUpdate();
        }
    }

    private void inserirTipoPessoa(
            Connection connection,
            UUID pessoaId,
            UUID tipoPessoaId,
            LocalDateTime now) throws SQLException {
        try (var statement = connection.prepareStatement("""
                INSERT INTO pessoa_tipo_pessoa (
                    id_pessoa_tipo_pessoa, id_pessoa, id_tipo_pessoa, created_at
                ) VALUES (?, ?, ?, ?)
                """)) {
            set(statement, UUID.randomUUID(), pessoaId, tipoPessoaId, now);
            statement.executeUpdate();
        }
    }

    private void inserirEndereco(
            Connection connection,
            UUID pessoaId,
            UUID tipoEnderecoId,
            AlunoNovo aluno,
            LocalDateTime now) throws SQLException {
        UUID enderecoId = UUID.randomUUID();
        try (var statement = connection.prepareStatement("""
                INSERT INTO endereco (
                    id_endereco, cep, logradouro, numero, complemento, bairro,
                    cidade, uf, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            set(statement, enderecoId, aluno.cep(), aluno.logradouro(), aluno.numero(),
                    aluno.complemento(), aluno.bairro(), aluno.cidade(), aluno.uf(), now);
            statement.executeUpdate();
        }
        try (var statement = connection.prepareStatement("""
                INSERT INTO pessoa_endereco (
                    id_pessoa_endereco, id_pessoa, id_endereco, id_tipo_endereco,
                    principal, created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            set(statement, UUID.randomUUID(), pessoaId, enderecoId, tipoEnderecoId, true, now);
            statement.executeUpdate();
        }
    }

    private void inserirAluno(
            Connection connection,
            UUID alunoId,
            UUID pessoaId,
            UUID statusId,
            AlunoNovo aluno,
            LocalDateTime now) throws SQLException {
        try (var statement = connection.prepareStatement("""
                INSERT INTO aluno (
                    id_aluno, id_pessoa, id_escola, id_status_aluno, nome_completo,
                    cpf, email, telefone, data_nascimento, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            set(statement, alunoId, pessoaId, aluno.escolaId(), statusId, aluno.nomeCompleto(),
                    aluno.cpf(), aluno.email(), aluno.telefone(), aluno.dataNascimento(), true, now);
            statement.executeUpdate();
        }
    }

    private AlunoCriado toCreated(UUID alunoId, AlunoNovo aluno, LocalDateTime now) {
        return new AlunoCriado(
                alunoId, aluno.nomeCompleto(), aluno.cpf(), aluno.email(), aluno.telefone(),
                aluno.dataNascimento(), aluno.rg(), aluno.orgaoEmissorRg(), aluno.ufRg(),
                aluno.nacionalidade(), aluno.naturalidade(), aluno.sexo(), aluno.nomeSocial(),
                aluno.cep(), aluno.logradouro(), aluno.numero(), aluno.complemento(), aluno.bairro(),
                aluno.cidade(), aluno.uf(), aluno.statusAluno(), aluno.escolaId(), aluno.escolaNome(), now);
    }

    private boolean enderecoInformado(AlunoNovo aluno) {
        return StringUtils.hasText(aluno.cep())
                || StringUtils.hasText(aluno.logradouro())
                || StringUtils.hasText(aluno.numero())
                || StringUtils.hasText(aluno.complemento())
                || StringUtils.hasText(aluno.bairro())
                || StringUtils.hasText(aluno.cidade())
                || StringUtils.hasText(aluno.uf());
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
