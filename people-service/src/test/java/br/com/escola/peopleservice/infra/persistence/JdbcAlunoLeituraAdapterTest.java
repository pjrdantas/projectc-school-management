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

import br.com.escola.peopleservice.application.model.AlunoNovo;
import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;

class JdbcAlunoLeituraAdapterTest {

    @Test
    void deveListarAlunosAtivosComFiltroPorNomeEEscola() {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        UUID outraEscolaId = UUID.randomUUID();
        criacaoAdapter(url).criar(novoAluno(escolaId, "Ana Maria", "12345678901", true));
        criacaoAdapter(url).criar(novoAluno(escolaId, "Bruno", "12345678902", false));
        criacaoAdapter(url).criar(novoAluno(outraEscolaId, "Ana Outra", "12345678903", false));

        var alunos = leituraAdapter(url).listar("ana", escolaId);

        assertThat(alunos).hasSize(1);
        assertThat(alunos.getFirst().nomeCompleto()).isEqualTo("Ana Maria");
        assertThat(alunos.getFirst().escolaId()).isEqualTo(escolaId);
    }

    @Test
    void deveBuscarDetalheCompletoDeAlunoAtivo() {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId, "Ana Maria", "12345678901", true));

        var aluno = leituraAdapter(url).buscar(criado.id(), escolaId);

        assertThat(aluno).isPresent();
        assertThat(aluno.get().cpf()).isEqualTo("12345678901");
        assertThat(aluno.get().logradouro()).isEqualTo("Praca da Se");
        assertThat(aluno.get().statusAluno()).isEqualTo("ATIVO");
        assertThat(aluno.get().escolaNome()).isEqualTo("Escola B3");
    }

    @Test
    void naoLeAlunoInativoOuDeOutraEscola() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId, "Ana Maria", "12345678901", false));
        execute(url, "UPDATE aluno SET ativo = FALSE WHERE id_aluno = ?", criado.id());

        assertThat(leituraAdapter(url).buscar(criado.id(), escolaId)).isEmpty();
        assertThat(leituraAdapter(url).listar(null, escolaId)).isEmpty();
        assertThat(leituraAdapter(url).buscar(criado.id(), UUID.randomUUID())).isEmpty();
    }

    @Test
    void deveBloquearLeituraQuandoHouverMaisDeUmEnderecoPrincipal() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId, "Ana Maria", "12345678901", true));
        UUID pessoaId = uuidValue(url, "SELECT id_pessoa FROM aluno WHERE id_aluno = ?", criado.id());
        inserirSegundoEnderecoPrincipal(url, pessoaId);

        assertThatThrownBy(() -> leituraAdapter(url).buscar(criado.id(), escolaId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mais de um endereco principal");
    }

    private JdbcAlunoCriacaoAdapter criacaoAdapter(String url) {
        return new JdbcAlunoCriacaoAdapter(properties(url));
    }

    private JdbcAlunoLeituraAdapter leituraAdapter(String url) {
        return new JdbcAlunoLeituraAdapter(properties(url));
    }

    private LeituraModeloMigrationProperties properties(String url) {
        return new LeituraModeloMigrationProperties(
                url, "sa", "", "org.h2.Driver", List.of("classpath:db/people-readmodel/migration"));
    }

    private AlunoNovo novoAluno(UUID escolaId, String nome, String cpf, boolean comEndereco) {
        return new AlunoNovo(
                nome, cpf, "aluno@escola.com", "11999999999", LocalDate.of(2015, 3, 10),
                "1234567", "SSP", "SP", "Brasileira", "Sao Paulo", "FEMININO", null,
                comEndereco ? "01001000" : null,
                comEndereco ? "Praca da Se" : null,
                comEndereco ? "10" : null,
                null, comEndereco ? "Se" : null, comEndereco ? "Sao Paulo" : null,
                comEndereco ? "SP" : null, "ATIVO", escolaId, "Escola B3");
    }

    private void prepararBanco(String url) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/people-readmodel/migration")
                .load()
                .migrate();
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            inserirCatalogo(connection, "tipo_pessoa", "id_tipo_pessoa", "ALUNO", "Aluno");
            inserirCatalogo(connection, "tipo_endereco", "id_tipo_endereco", "RESIDENCIAL", "Residencial");
            inserirCatalogo(connection, "status_aluno", "id_status_aluno", "ATIVO", "Ativo");
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void inserirCatalogo(
            Connection connection,
            String tabela,
            String colunaId,
            String codigo,
            String descricao) throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO " + tabela + " (" + colunaId + ", codigo, descricao) VALUES (?, ?, ?)")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, codigo);
            statement.setString(3, descricao);
            statement.executeUpdate();
        }
    }

    private void inserirSegundoEnderecoPrincipal(String url, UUID pessoaId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            UUID enderecoId = UUID.randomUUID();
            UUID tipoEnderecoId = uuidValue(connection, "SELECT id_tipo_endereco FROM tipo_endereco WHERE codigo = 'RESIDENCIAL'");
            try (var statement = connection.prepareStatement("""
                    INSERT INTO endereco (id_endereco, cep, logradouro, numero, cidade, uf)
                    VALUES (?, '02002000', 'Rua Segunda', '20', 'Sao Paulo', 'SP')
                    """)) {
                statement.setObject(1, enderecoId);
                statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("""
                    INSERT INTO pessoa_endereco (
                        id_pessoa_endereco, id_pessoa, id_endereco, id_tipo_endereco, principal
                    ) VALUES (?, ?, ?, ?, TRUE)
                    """)) {
                set(statement, UUID.randomUUID(), pessoaId, enderecoId, tipoEnderecoId);
                statement.executeUpdate();
            }
        }
    }

    private UUID uuidValue(String url, String sql, UUID id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getObject(1, UUID.class);
            }
        }
    }

    private UUID uuidValue(Connection connection, String sql) throws SQLException {
        try (var statement = connection.prepareStatement(sql);
                var resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getObject(1, UUID.class);
        }
    }

    private void execute(String url, String sql, UUID id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        }
    }

    private void set(java.sql.PreparedStatement statement, Object... values) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            statement.setObject(index + 1, values[index]);
        }
    }

    private String databaseUrl() {
        return "jdbc:h2:mem:people_student_read_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }
}
