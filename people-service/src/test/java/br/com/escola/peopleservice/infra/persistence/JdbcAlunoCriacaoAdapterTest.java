package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.exception.ConflitoPessoaException;
import br.com.escola.peopleservice.application.model.AlunoNovo;
import br.com.escola.peopleservice.infra.config.PeoplePersistenceProperties;

class JdbcAlunoCriacaoAdapterTest {

    @Test
    void deveCriarPessoaAlunoEEnderecoNaMesmaOperacao() throws SQLException {
        String url = databaseUrl();
        migrate(url);
        prepararCatalogos(url, true);
        JdbcAlunoCriacaoAdapter adapter = adapter(url);
        AlunoNovo aluno = aluno();

        var criado = adapter.criar(aluno);

        assertThat(criado.id()).isNotNull();
        assertThat(criado.escolaNome()).isEqualTo("Escola B3");
        assertThat(count(url, "pessoa")).isOne();
        assertThat(count(url, "pessoa_tipo_pessoa")).isOne();
        assertThat(count(url, "endereco")).isOne();
        assertThat(count(url, "pessoa_endereco")).isOne();
        assertThat(count(url, "aluno")).isOne();

        assertThatThrownBy(() -> adapter.criar(aluno))
                .isInstanceOf(ConflitoPessoaException.class);
        assertThat(count(url, "pessoa")).isOne();
        assertThat(count(url, "aluno")).isOne();
    }

    @Test
    void deveManterBancoSemEscritasQuandoCatalogoObrigatorioNaoExiste() throws SQLException {
        String url = databaseUrl();
        migrate(url);
        prepararCatalogos(url, false);

        assertThatThrownBy(() -> adapter(url).criar(aluno()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ATIVO");
        assertThat(count(url, "pessoa")).isZero();
        assertThat(count(url, "aluno")).isZero();
        assertThat(count(url, "endereco")).isZero();
    }

    private JdbcAlunoCriacaoAdapter adapter(String url) {
        return new JdbcAlunoCriacaoAdapter(new PeoplePersistenceProperties(
                url,
                "sa",
                "",
                "org.h2.Driver",
                List.of("classpath:db/people/migration")));
    }

    private AlunoNovo aluno() {
        return new AlunoNovo(
                "Aluno B3", "12345678901", "aluno@escola.com", "11999999999",
                LocalDate.of(2015, 3, 10), "1234567", "SSP", "SP", "Brasileira",
                "Sao Paulo", "MASCULINO", null, "01001000", "Praca da Se", "10",
                null, "Se", "Sao Paulo", "SP", "ATIVO", UUID.randomUUID(), "Escola B3");
    }

    private void prepararCatalogos(String url, boolean incluirStatus) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            insertCatalog(connection, "tipo_pessoa", "id_tipo_pessoa", "ALUNO", "Aluno");
            insertCatalog(connection, "tipo_endereco", "id_tipo_endereco", "RESIDENCIAL", "Residencial");
            if (incluirStatus) {
                insertCatalog(connection, "status_aluno", "id_status_aluno", "ATIVO", "Ativo");
            }
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

    private int count(String url, String table) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement("SELECT COUNT(1) FROM " + table);
                var resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private void migrate(String url) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/people/migration")
                .load()
                .migrate();
    }

    private String databaseUrl() {
        return "jdbc:h2:mem:people_student_create_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }
}
