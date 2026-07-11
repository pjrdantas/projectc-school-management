package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

class JdbcPessoaCatalogoAdapterTest {

    @Test
    void deveLerCatalogosLocaisOrdenadosPorCodigo() throws Exception {
        String url = h2Url("local_read_" + UUID.randomUUID());
        criarSchemaEPopular(url);
        JdbcPessoaCatalogoAdapter adapter = new JdbcPessoaCatalogoAdapter(
                new PeopleReadModelMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var tiposPessoa = adapter.listarTiposPessoa();
        var tiposEndereco = adapter.listarTiposEndereco();
        var statusAluno = adapter.listarStatusAluno();
        var parentescos = adapter.listarParentescos();

        assertThat(tiposPessoa)
                .extracting("codigo")
                .containsExactly("ALUNO", "PROFESSOR");
        assertThat(tiposEndereco)
                .extracting("codigo")
                .containsExactly("COMERCIAL", "RESIDENCIAL");
        assertThat(statusAluno)
                .extracting("codigo")
                .containsExactly("ATIVO", "INATIVO");
        assertThat(parentescos)
                .extracting("codigo")
                .containsExactly("MAE", "PAI");
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchemaEPopular(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE tipo_pessoa (
                        id_tipo_pessoa UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL UNIQUE,
                        descricao VARCHAR(120) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE tipo_endereco (
                        id_tipo_endereco UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL UNIQUE,
                        descricao VARCHAR(120) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE status_aluno (
                        id_status_aluno UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL UNIQUE,
                        descricao VARCHAR(120) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE parentesco (
                        id_parentesco UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL UNIQUE,
                        descricao VARCHAR(120) NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO tipo_pessoa (id_tipo_pessoa, codigo, descricao, created_at) VALUES
                    ('22222222-2222-2222-2222-222222222222', 'PROFESSOR', 'Professor', CURRENT_TIMESTAMP),
                    ('11111111-1111-1111-1111-111111111111', 'ALUNO', 'Aluno', CURRENT_TIMESTAMP)
                    """);
            statement.execute("""
                    INSERT INTO tipo_endereco (id_tipo_endereco, codigo, descricao) VALUES
                    ('55555555-5555-5555-5555-555555555555', 'RESIDENCIAL', 'Residencial'),
                    ('44444444-4444-4444-4444-444444444444', 'COMERCIAL', 'Comercial')
                    """);
            statement.execute("""
                    INSERT INTO status_aluno (id_status_aluno, codigo, descricao) VALUES
                    ('66666666-6666-6666-6666-666666666666', 'INATIVO', 'Inativo'),
                    ('77777777-7777-7777-7777-777777777777', 'ATIVO', 'Ativo')
                    """);
            statement.execute("""
                    INSERT INTO parentesco (id_parentesco, codigo, descricao) VALUES
                    ('88888888-8888-8888-8888-888888888888', 'PAI', 'Pai'),
                    ('99999999-9999-9999-9999-999999999999', 'MAE', 'Mae')
                    """);
        }
    }
}

