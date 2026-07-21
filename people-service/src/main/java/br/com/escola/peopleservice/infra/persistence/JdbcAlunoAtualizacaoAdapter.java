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
import br.com.escola.peopleservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.peopleservice.application.model.AlunoAlteracao;
import br.com.escola.peopleservice.application.model.AlunoAtualizado;
import br.com.escola.peopleservice.application.port.out.AlunoAtualizacaoPort;
import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;

@Component
public class JdbcAlunoAtualizacaoAdapter implements AlunoAtualizacaoPort {

    private static final String TIPO_ENDERECO_RESIDENCIAL = "RESIDENCIAL";

    private final LeituraModeloMigrationProperties properties;

    public JdbcAlunoAtualizacaoAdapter(LeituraModeloMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public AlunoAtualizado atualizar(UUID alunoId, AlunoAlteracao aluno) {
        validarConfiguracao();
        carregarDriver();
        try (Connection connection = DriverManager.getConnection(
                properties.url(), properties.username(), properties.password())) {
            connection.setAutoCommit(false);
            try {
                AlunoAtualizado atualizado = persistir(connection, alunoId, aluno);
                connection.commit();
                return atualizado;
            } catch (RuntimeException | SQLException exception) {
                rollback(connection, exception);
                if (exception instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (exception instanceof SQLException sqlException
                        && "23505".equals(sqlException.getSQLState())) {
                    throw new ConflitoPessoaException("Aluno ja cadastrado com o CPF informado");
                }
                throw new IllegalStateException("Falha ao atualizar aluno no banco de pessoas", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao acessar banco de pessoas", exception);
        }
    }

    private AlunoAtualizado persistir(Connection connection, UUID alunoId, AlunoAlteracao aluno)
            throws SQLException {
        AlunoPersistido persistido = buscarAluno(connection, alunoId, aluno.escolaId());
        if (cpfPertenceAOutroAluno(connection, alunoId, aluno.cpf(), aluno.escolaId())) {
            throw new ConflitoPessoaException("Aluno ja cadastrado com o CPF informado");
        }
        UUID statusId = catalogoId(connection, "status_aluno", "id_status_aluno", aluno.statusAluno());
        LocalDateTime now = LocalDateTime.now();

        atualizarPessoa(connection, persistido.pessoaId(), aluno, now);
        atualizarAluno(connection, alunoId, statusId, aluno, now);
        EnderecoPersistido endereco = atualizarEnderecoPrincipal(connection, persistido.pessoaId(), aluno, now);
        return toUpdated(alunoId, aluno, persistido, endereco);
    }

    private AlunoPersistido buscarAluno(Connection connection, UUID alunoId, UUID escolaId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT a.id_pessoa, a.created_at, p.escola_nome,
                       e.cep, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.uf
                FROM aluno a
                JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                LEFT JOIN pessoa_endereco pe ON pe.id_pessoa = p.id_pessoa AND pe.principal = TRUE
                LEFT JOIN endereco e ON e.id_endereco = pe.id_endereco
                WHERE a.id_aluno = ? AND a.id_escola = ? AND p.id_escola = ?
                  AND a.ativo = TRUE AND p.ativo = TRUE
                """)) {
            set(statement, alunoId, escolaId, escolaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new RecursoNaoEncontradoException("Aluno nao encontrado");
                }
                AlunoPersistido persistido = new AlunoPersistido(
                        resultSet.getObject("id_pessoa", UUID.class),
                        resultSet.getString("escola_nome"),
                        resultSet.getObject("created_at", LocalDateTime.class),
                        new EnderecoPersistido(
                                resultSet.getString("cep"), resultSet.getString("logradouro"),
                                resultSet.getString("numero"), resultSet.getString("complemento"),
                                resultSet.getString("bairro"), resultSet.getString("cidade"),
                                resultSet.getString("uf")));
                if (resultSet.next()) {
                    throw new IllegalStateException("Aluno possui mais de um endereco principal");
                }
                return persistido;
            }
        }
    }

    private boolean cpfPertenceAOutroAluno(
            Connection connection,
            UUID alunoId,
            String cpf,
            UUID escolaId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT COUNT(1)
                FROM aluno a
                JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                WHERE a.id_aluno <> ? AND a.id_escola = ? AND p.id_escola = ? AND p.cpf = ?
                """)) {
            set(statement, alunoId, escolaId, escolaId, cpf);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private void atualizarPessoa(
            Connection connection,
            UUID pessoaId,
            AlunoAlteracao aluno,
            LocalDateTime now) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE pessoa
                SET nome_completo = ?, cpf = ?, rg = ?, orgao_emissor_rg = ?, uf_rg = ?,
                    email = ?, telefone = ?, data_nascimento = ?, sexo = ?, nome_social = ?,
                    nacionalidade = ?, naturalidade = ?, ativo = TRUE, updated_at = ?
                WHERE id_pessoa = ? AND id_escola = ?
                """)) {
            set(statement, aluno.nomeCompleto(), aluno.cpf(), aluno.rg(), aluno.orgaoEmissorRg(),
                    aluno.ufRg(), aluno.email(), aluno.telefone(), aluno.dataNascimento(), aluno.sexo(),
                    aluno.nomeSocial(), aluno.nacionalidade(), aluno.naturalidade(), now,
                    pessoaId, aluno.escolaId());
            if (statement.executeUpdate() != 1) {
                throw new RecursoNaoEncontradoException("Pessoa do aluno nao encontrada");
            }
        }
    }

    private void atualizarAluno(
            Connection connection,
            UUID alunoId,
            UUID statusId,
            AlunoAlteracao aluno,
            LocalDateTime now) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE aluno
                SET id_status_aluno = ?, nome_completo = ?, cpf = ?, email = ?, telefone = ?,
                    data_nascimento = ?, ativo = TRUE, updated_at = ?
                WHERE id_aluno = ? AND id_escola = ?
                """)) {
            set(statement, statusId, aluno.nomeCompleto(), aluno.cpf(), aluno.email(), aluno.telefone(),
                    aluno.dataNascimento(), now, alunoId, aluno.escolaId());
            if (statement.executeUpdate() != 1) {
                throw new RecursoNaoEncontradoException("Aluno nao encontrado");
            }
        }
    }

    private EnderecoPersistido atualizarEnderecoPrincipal(
            Connection connection,
            UUID pessoaId,
            AlunoAlteracao aluno,
            LocalDateTime now) throws SQLException {
        if (!enderecoInformado(aluno)) {
            return null;
        }
        UUID tipoEnderecoId = catalogoId(
                connection, "tipo_endereco", "id_tipo_endereco", TIPO_ENDERECO_RESIDENCIAL);
        UUID enderecoId = buscarEnderecoPrincipalId(connection, pessoaId);
        if (enderecoId == null) {
            inserirEnderecoPrincipal(connection, pessoaId, tipoEnderecoId, aluno, now);
        } else {
            atualizarEndereco(connection, enderecoId, aluno, now);
            atualizarVinculoEndereco(connection, pessoaId, tipoEnderecoId);
        }
        return toEndereco(aluno);
    }

    private UUID buscarEnderecoPrincipalId(Connection connection, UUID pessoaId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT id_endereco FROM pessoa_endereco
                WHERE id_pessoa = ? AND principal = TRUE
                ORDER BY created_at DESC
                """)) {
            statement.setObject(1, pessoaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getObject("id_endereco", UUID.class) : null;
            }
        }
    }

    private void inserirEnderecoPrincipal(
            Connection connection,
            UUID pessoaId,
            UUID tipoEnderecoId,
            AlunoAlteracao aluno,
            LocalDateTime now) throws SQLException {
        UUID enderecoId = UUID.randomUUID();
        try (var statement = connection.prepareStatement("""
                INSERT INTO endereco (
                    id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            set(statement, enderecoId, aluno.cep(), aluno.logradouro(), aluno.numero(), aluno.complemento(),
                    aluno.bairro(), aluno.cidade(), aluno.uf(), now);
            statement.executeUpdate();
        }
        try (var statement = connection.prepareStatement("""
                INSERT INTO pessoa_endereco (
                    id_pessoa_endereco, id_pessoa, id_endereco, id_tipo_endereco, principal, created_at
                ) VALUES (?, ?, ?, ?, TRUE, ?)
                """)) {
            set(statement, UUID.randomUUID(), pessoaId, enderecoId, tipoEnderecoId, now);
            statement.executeUpdate();
        }
    }

    private void atualizarEndereco(
            Connection connection,
            UUID enderecoId,
            AlunoAlteracao aluno,
            LocalDateTime now) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE endereco
                SET cep = ?, logradouro = ?, numero = ?, complemento = ?, bairro = ?,
                    cidade = ?, uf = ?, updated_at = ?
                WHERE id_endereco = ?
                """)) {
            set(statement, aluno.cep(), aluno.logradouro(), aluno.numero(), aluno.complemento(),
                    aluno.bairro(), aluno.cidade(), aluno.uf(), now, enderecoId);
            statement.executeUpdate();
        }
    }

    private void atualizarVinculoEndereco(
            Connection connection,
            UUID pessoaId,
            UUID tipoEnderecoId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE pessoa_endereco SET id_tipo_endereco = ?, principal = TRUE
                WHERE id_pessoa = ? AND principal = TRUE
                """)) {
            set(statement, tipoEnderecoId, pessoaId);
            statement.executeUpdate();
        }
    }

    private UUID catalogoId(Connection connection, String table, String idColumn, String code)
            throws SQLException {
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

    private AlunoAtualizado toUpdated(
            UUID alunoId,
            AlunoAlteracao aluno,
            AlunoPersistido persistido,
            EnderecoPersistido enderecoAtualizado) {
        EnderecoPersistido endereco = enderecoAtualizado == null ? persistido.endereco() : enderecoAtualizado;
        return new AlunoAtualizado(
                alunoId, aluno.nomeCompleto(), aluno.cpf(), aluno.email(), aluno.telefone(),
                aluno.dataNascimento(), aluno.rg(), aluno.orgaoEmissorRg(), aluno.ufRg(),
                aluno.nacionalidade(), aluno.naturalidade(), aluno.sexo(), aluno.nomeSocial(),
                endereco.cep(), endereco.logradouro(), endereco.numero(), endereco.complemento(),
                endereco.bairro(), endereco.cidade(), endereco.uf(), aluno.statusAluno(),
                aluno.escolaId(), persistido.escolaNome(), persistido.createdAt());
    }

    private EnderecoPersistido toEndereco(AlunoAlteracao aluno) {
        return new EnderecoPersistido(
                aluno.cep(), aluno.logradouro(), aluno.numero(), aluno.complemento(),
                aluno.bairro(), aluno.cidade(), aluno.uf());
    }

    private boolean enderecoInformado(AlunoAlteracao aluno) {
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

    private record AlunoPersistido(
            UUID pessoaId,
            String escolaNome,
            LocalDateTime createdAt,
            EnderecoPersistido endereco) {
    }

    private record EnderecoPersistido(
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf) {
    }
}
