package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.exception.ConflitoPessoaException;
import br.com.escola.peopleservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.peopleservice.application.model.AlunoAlteracao;
import br.com.escola.peopleservice.application.model.AlunoNovo;
import br.com.escola.peopleservice.infra.config.PeoplePersistenceProperties;

class JdbcAlunoAtualizacaoAdapterTest {

    @Test
    void deveAtualizarPessoaContatoAlunoEEnderecoPreservandoIdentidade() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId, "12345678901", true));
        LocalDateTime createdAtPersistido = ((Timestamp) value(
                url, "SELECT created_at FROM aluno WHERE id_aluno = ?", criado.id())).toLocalDateTime();

        var atualizado = atualizacaoAdapter(url).atualizar(
                criado.id(), alteracao(escolaId, "98765432100", true));

        assertThat(atualizado.id()).isEqualTo(criado.id());
        assertThat(atualizado.createdAt()).isEqualTo(createdAtPersistido);
        assertThat(atualizado.nomeCompleto()).isEqualTo("Aluno Atualizado");
        assertThat(atualizado.email()).isEqualTo("novo@escola.com");
        assertThat(atualizado.statusAluno()).isEqualTo("INATIVO");
        assertThat(atualizado.logradouro()).isEqualTo("Rua Atualizada");
        assertThat(value(url, "SELECT nome_completo FROM pessoa WHERE id_pessoa = (SELECT id_pessoa FROM aluno WHERE id_aluno = ?)", criado.id()))
                .isEqualTo("Aluno Atualizado");
        assertThat(value(url, "SELECT email FROM aluno WHERE id_aluno = ?", criado.id()))
                .isEqualTo("novo@escola.com");
        assertThat(value(url, "SELECT logradouro FROM endereco", null)).isEqualTo("Rua Atualizada");
        assertThat(count(url, "endereco")).isOne();
    }

    @Test
    void devePreservarEnderecoExistenteQuandoNaoVierNaAtualizacao() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId, "12345678901", true));

        var atualizado = atualizacaoAdapter(url).atualizar(
                criado.id(), alteracao(escolaId, "12345678901", false));

        assertThat(atualizado.cep()).isEqualTo("01001000");
        assertThat(atualizado.logradouro()).isEqualTo("Praca da Se");
        assertThat(count(url, "endereco")).isOne();
    }

    @Test
    void deveCriarEnderecoPrincipalQuandoAlunoAindaNaoPossuiEndereco() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId, "12345678901", false));

        var atualizado = atualizacaoAdapter(url).atualizar(
                criado.id(), alteracao(escolaId, "12345678901", true));

        assertThat(atualizado.cep()).isEqualTo("02002000");
        assertThat(count(url, "endereco")).isOne();
        assertThat(count(url, "pessoa_endereco")).isOne();
    }

    @Test
    void deveBloquearCpfDeOutroAlunoSemAlterarDados() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var primeiro = criacaoAdapter(url).criar(novoAluno(escolaId, "12345678901", true));
        criacaoAdapter(url).criar(novoAluno(escolaId, "98765432100", false));

        assertThatThrownBy(() -> atualizacaoAdapter(url).atualizar(
                primeiro.id(), alteracao(escolaId, "98765432100", true)))
                .isInstanceOf(ConflitoPessoaException.class);
        assertThat(value(url, "SELECT nome_completo FROM aluno WHERE id_aluno = ?", primeiro.id()))
                .isEqualTo("Aluno Original");
    }

    @Test
    void deveRetornarNaoEncontradoSemEscrever() {
        String url = databaseUrl();
        prepararBanco(url);

        assertThatThrownBy(() -> atualizacaoAdapter(url).atualizar(
                UUID.randomUUID(), alteracao(UUID.randomUUID(), "12345678901", true)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveReverterPessoaEAlunoQuandoEnderecoFalhar() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId, "12345678901", false));
        execute(url, "DELETE FROM tipo_endereco");

        assertThatThrownBy(() -> atualizacaoAdapter(url).atualizar(
                criado.id(), alteracao(escolaId, "12345678901", true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RESIDENCIAL");
        assertThat(value(url, "SELECT nome_completo FROM pessoa WHERE id_pessoa = (SELECT id_pessoa FROM aluno WHERE id_aluno = ?)", criado.id()))
                .isEqualTo("Aluno Original");
        assertThat(value(url, "SELECT nome_completo FROM aluno WHERE id_aluno = ?", criado.id()))
                .isEqualTo("Aluno Original");
        assertThat(count(url, "endereco")).isZero();
    }

    private JdbcAlunoCriacaoAdapter criacaoAdapter(String url) {
        return new JdbcAlunoCriacaoAdapter(properties(url));
    }

    private JdbcAlunoAtualizacaoAdapter atualizacaoAdapter(String url) {
        return new JdbcAlunoAtualizacaoAdapter(properties(url));
    }

    private PeoplePersistenceProperties properties(String url) {
        return new PeoplePersistenceProperties(
                url, "sa", "", "org.h2.Driver", List.of("classpath:db/people/migration"));
    }

    private AlunoNovo novoAluno(UUID escolaId, String cpf, boolean comEndereco) {
        return new AlunoNovo(
                "Aluno Original", cpf, "original@escola.com", "11999999999",
                LocalDate.of(2015, 3, 10), "1234567", "SSP", "SP", "Brasileira",
                "Sao Paulo", "MASCULINO", null,
                comEndereco ? "01001000" : null,
                comEndereco ? "Praca da Se" : null,
                comEndereco ? "10" : null,
                null, comEndereco ? "Se" : null, comEndereco ? "Sao Paulo" : null,
                comEndereco ? "SP" : null, "ATIVO", escolaId, "Escola B3");
    }

    private AlunoAlteracao alteracao(UUID escolaId, String cpf, boolean comEndereco) {
        return new AlunoAlteracao(
                "Aluno Atualizado", cpf, "novo@escola.com", "11888888888",
                LocalDate.of(2014, 4, 11), "7654321", "SSP", "SP", "Brasileira",
                "Campinas", "FEMININO", "Nome Social",
                comEndereco ? "02002000" : null,
                comEndereco ? "Rua Atualizada" : null,
                comEndereco ? "20" : null,
                null, comEndereco ? "Centro" : null, comEndereco ? "Sao Paulo" : null,
                comEndereco ? "SP" : null, "INATIVO", escolaId);
    }

    private void prepararBanco(String url) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/people/migration")
                .load()
                .migrate();
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            insertCatalog(connection, "tipo_pessoa", "id_tipo_pessoa", "ALUNO", "Aluno");
            insertCatalog(connection, "tipo_endereco", "id_tipo_endereco", "RESIDENCIAL", "Residencial");
            insertCatalog(connection, "status_aluno", "id_status_aluno", "ATIVO", "Ativo");
            insertCatalog(connection, "status_aluno", "id_status_aluno", "INATIVO", "Inativo");
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void insertCatalog(
            Connection connection,
            String table,
            String idColumn,
            String code,
            String description) throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO " + table + " (" + idColumn + ", codigo, descricao) VALUES (?, ?, ?)")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, code);
            statement.setString(3, description);
            statement.executeUpdate();
        }
    }

    private Object value(String url, String sql, UUID id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement(sql)) {
            if (id != null) {
                statement.setObject(1, id);
            }
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getObject(1);
            }
        }
    }

    private int count(String url, String table) throws SQLException {
        return ((Number) value(url, "SELECT COUNT(1) FROM " + table, null)).intValue();
    }

    private void execute(String url, String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private String databaseUrl() {
        return "jdbc:h2:mem:people_student_update_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }
}
