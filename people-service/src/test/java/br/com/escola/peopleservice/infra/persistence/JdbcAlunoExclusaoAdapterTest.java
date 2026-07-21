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

import br.com.escola.peopleservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.peopleservice.application.model.AlunoAlteracao;
import br.com.escola.peopleservice.application.model.AlunoNovo;
import br.com.escola.peopleservice.infra.config.LeituraModeloMigrationProperties;

class JdbcAlunoExclusaoAdapterTest {

    @Test
    void deveInativarAlunoEPessoaPreservandoDependenciasLocais() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId));
        UUID pessoaId = uuidValue(url, "SELECT id_pessoa FROM aluno WHERE id_aluno = ?", criado.id());
        inserirDocumento(url, pessoaId, escolaId);
        inserirVinculoResponsavel(url, criado.id(), escolaId);

        exclusaoAdapter(url).excluir(criado.id(), escolaId);

        assertThat(booleanValue(url, "SELECT ativo FROM aluno WHERE id_aluno = ?", criado.id())).isFalse();
        assertThat(booleanValue(url, "SELECT ativo FROM pessoa WHERE id_pessoa = ?", pessoaId)).isFalse();
        assertThat(value(url, "SELECT data_saida FROM aluno WHERE id_aluno = ?", criado.id())).isNotNull();
        assertThat(value(url, "SELECT motivo_saida FROM aluno WHERE id_aluno = ?", criado.id()))
                .isEqualTo("EXCLUSAO_SOLICITADA");
        assertThat(count(url, "aluno")).isOne();
        assertThat(count(url, "pessoa_endereco")).isOne();
        assertThat(count(url, "people_documento_read_model")).isOne();
        assertThat(count(url, "aluno_responsavel")).isOne();
        assertThat(new JdbcAlunoPessoaAdapter(properties(url)).buscarVinculoPorAlunoId(criado.id(), escolaId))
                .isEmpty();
        assertThatThrownBy(() -> atualizacaoAdapter(url).atualizar(
                criado.id(), alteracao(escolaId)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveManterPessoaAtivaQuandoElaPossuiOutroTipo() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId));
        UUID pessoaId = uuidValue(url, "SELECT id_pessoa FROM aluno WHERE id_aluno = ?", criado.id());
        vincularOutroTipo(url, pessoaId);

        exclusaoAdapter(url).excluir(criado.id(), escolaId);

        assertThat(booleanValue(url, "SELECT ativo FROM aluno WHERE id_aluno = ?", criado.id())).isFalse();
        assertThat(booleanValue(url, "SELECT ativo FROM pessoa WHERE id_pessoa = ?", pessoaId)).isTrue();
        assertThat(count(url, "pessoa_tipo_pessoa")).isEqualTo(2);
    }

    @Test
    void deveBloquearEscolaDiferenteEExclusaoRepetida() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId));

        assertThatThrownBy(() -> exclusaoAdapter(url).excluir(criado.id(), UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(booleanValue(url, "SELECT ativo FROM aluno WHERE id_aluno = ?", criado.id())).isTrue();

        exclusaoAdapter(url).excluir(criado.id(), escolaId);
        assertThatThrownBy(() -> exclusaoAdapter(url).excluir(criado.id(), escolaId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveReverterInativacaoDoAlunoQuandoPessoaNaoPuderSerInativada() throws SQLException {
        String url = databaseUrl();
        prepararBanco(url);
        UUID escolaId = UUID.randomUUID();
        var criado = criacaoAdapter(url).criar(novoAluno(escolaId));
        execute(url, "ALTER TABLE pessoa ADD CONSTRAINT ck_pessoa_ativa CHECK (ativo = TRUE)");

        assertThatThrownBy(() -> exclusaoAdapter(url).excluir(criado.id(), escolaId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("excluir aluno");
        assertThat(booleanValue(url, "SELECT ativo FROM aluno WHERE id_aluno = ?", criado.id())).isTrue();
        assertThat(value(url, "SELECT data_saida FROM aluno WHERE id_aluno = ?", criado.id())).isNull();
    }

    private JdbcAlunoCriacaoAdapter criacaoAdapter(String url) {
        return new JdbcAlunoCriacaoAdapter(properties(url));
    }

    private JdbcAlunoAtualizacaoAdapter atualizacaoAdapter(String url) {
        return new JdbcAlunoAtualizacaoAdapter(properties(url));
    }

    private JdbcAlunoExclusaoAdapter exclusaoAdapter(String url) {
        return new JdbcAlunoExclusaoAdapter(properties(url));
    }

    private LeituraModeloMigrationProperties properties(String url) {
        return new LeituraModeloMigrationProperties(
                url, "sa", "", "org.h2.Driver", List.of("classpath:db/people-readmodel/migration"));
    }

    private AlunoNovo novoAluno(UUID escolaId) {
        return new AlunoNovo(
                "Aluno Exclusao", "12345678901", "aluno@escola.com", "11999999999",
                LocalDate.of(2015, 3, 10), "1234567", "SSP", "SP", "Brasileira",
                "Sao Paulo", "MASCULINO", null, "01001000", "Praca da Se", "10",
                null, "Se", "Sao Paulo", "SP", "ATIVO", escolaId, "Escola B3");
    }

    private AlunoAlteracao alteracao(UUID escolaId) {
        return new AlunoAlteracao(
                "Aluno Alterado", "12345678901", "novo@escola.com", "11888888888",
                LocalDate.of(2014, 4, 11), null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, "ATIVO", escolaId);
    }

    private void prepararBanco(String url) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/people-readmodel/migration")
                .load()
                .migrate();
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            insertCatalog(connection, "tipo_pessoa", "id_tipo_pessoa", "ALUNO", "Aluno");
            insertCatalog(connection, "tipo_endereco", "id_tipo_endereco", "RESIDENCIAL", "Residencial");
            insertCatalog(connection, "status_aluno", "id_status_aluno", "ATIVO", "Ativo");
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void inserirDocumento(String url, UUID pessoaId, UUID escolaId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement("""
                        INSERT INTO people_documento_read_model (
                            id_pessoa_documento, id_pessoa, id_documento, id_tipo_documento,
                            tipo_documento_codigo, tipo_documento_descricao, caminho_arquivo, id_escola
                        ) VALUES (?, ?, ?, ?, 'RG', 'RG', '/arquivo/rg.pdf', ?)
                        """)) {
            set(statement, UUID.randomUUID(), pessoaId, UUID.randomUUID(), UUID.randomUUID(), escolaId);
            statement.executeUpdate();
        }
    }

    private void inserirVinculoResponsavel(String url, UUID alunoId, UUID escolaId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            UUID pessoaId = UUID.randomUUID();
            UUID responsavelId = UUID.randomUUID();
            try (var statement = connection.prepareStatement("""
                    INSERT INTO pessoa (
                        id_pessoa, id_escola, escola_nome, nome_completo, cpf, ativo
                    ) VALUES (?, ?, 'Escola B3', 'Responsavel', '98765432100', TRUE)
                    """)) {
                set(statement, pessoaId, escolaId);
                statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("""
                    INSERT INTO responsavel (id_responsavel, id_pessoa, nome_completo, cpf)
                    VALUES (?, ?, 'Responsavel', '98765432100')
                    """)) {
                set(statement, responsavelId, pessoaId);
                statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("""
                    INSERT INTO aluno_responsavel (
                        id_aluno_responsavel, id_aluno, id_responsavel
                    ) VALUES (?, ?, ?)
                    """)) {
                set(statement, UUID.randomUUID(), alunoId, responsavelId);
                statement.executeUpdate();
            }
        }
    }

    private void vincularOutroTipo(String url, UUID pessoaId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            UUID tipoId = UUID.randomUUID();
            try (var statement = connection.prepareStatement("""
                    INSERT INTO tipo_pessoa (id_tipo_pessoa, codigo, descricao)
                    VALUES (?, 'PROFESSOR', 'Professor')
                    """)) {
                statement.setObject(1, tipoId);
                statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("""
                    INSERT INTO pessoa_tipo_pessoa (
                        id_pessoa_tipo_pessoa, id_pessoa, id_tipo_pessoa
                    ) VALUES (?, ?, ?)
                    """)) {
                set(statement, UUID.randomUUID(), pessoaId, tipoId);
                statement.executeUpdate();
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
            set(statement, UUID.randomUUID(), code, description);
            statement.executeUpdate();
        }
    }

    private UUID uuidValue(String url, String sql, UUID id) throws SQLException {
        return (UUID) value(url, sql, id);
    }

    private boolean booleanValue(String url, String sql, UUID id) throws SQLException {
        return (Boolean) value(url, sql, id);
    }

    private Object value(String url, String sql, UUID id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getObject(1);
            }
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

    private void execute(String url, String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private void set(java.sql.PreparedStatement statement, Object... values) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            statement.setObject(index + 1, values[index]);
        }
    }

    private String databaseUrl() {
        return "jdbc:h2:mem:people_student_delete_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }
}
