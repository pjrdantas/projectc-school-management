package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleReadModelSourceProperties;
import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

class JdbcPeopleCatalogReadModelSyncAdapterTest {

    @Test
    void bloqueiaSincronizacaoQuandoOrigemOuDestinoNaoEstaoConfigurados() {
        JdbcPeopleCatalogReadModelSyncAdapter adapter = new JdbcPeopleCatalogReadModelSyncAdapter(
                new PeopleReadModelSourceProperties("", "", "", ""),
                new PeopleReadModelMigrationProperties("", "", "", "", List.of()));

        var reports = adapter.synchronize(true, true, 100);

        assertThat(reports)
                .hasSize(11)
                .allSatisfy(report -> {
                    assertThat(report.status()).isEqualTo("blocked");
                    assertThat(report.reason()).isEqualTo("local-read-model-source-url-required");
                    assertThat(report.backfillPlanned()).isTrue();
                    assertThat(report.reconciliationPlanned()).isTrue();
                });
    }

    @Test
    void copiaEReconciliaCatalogosEIdentidadeDoMonolitoParaSchemaLocal() throws Exception {
        String sourceUrl = h2Url("source_" + UUID.randomUUID());
        String targetUrl = h2Url("target_" + UUID.randomUUID());
        criarSchema(sourceUrl);
        criarSchema(targetUrl);
        popularOrigem(sourceUrl);

        JdbcPeopleCatalogReadModelSyncAdapter adapter = new JdbcPeopleCatalogReadModelSyncAdapter(
                new PeopleReadModelSourceProperties(sourceUrl, "sa", "", "org.h2.Driver"),
                new PeopleReadModelMigrationProperties(targetUrl, "sa", "", "org.h2.Driver", List.of()));

        var reports = adapter.synchronize(true, true, 100);

        assertThat(reports)
                .hasSize(11)
                .allSatisfy(report -> {
                    assertThat(report.status()).isEqualTo("success");
                    assertThat(report.divergences()).isZero();
                    assertThat(report.backfilledRecords()).isGreaterThan(0);
                });
        assertThat(reports)
                .extracting("table")
                .containsExactly(
                        "tipo_pessoa",
                        "tipo_endereco",
                        "pessoa",
                        "pessoa_tipo_pessoa",
                        "aluno",
                        "responsavel",
                        "aluno_responsavel",
                        "endereco",
                        "pessoa_endereco",
                        "people_documento_read_model",
                        "people_funcionario_read_model");
        assertThat(reports.stream().mapToInt(report -> report.sourceRows()).sum()).isEqualTo(20);
        assertThat(reports.stream().mapToInt(report -> report.targetRows()).sum()).isEqualTo(20);

        assertThat(contar(targetUrl, "tipo_pessoa")).isEqualTo(3);
        assertThat(contar(targetUrl, "tipo_endereco")).isEqualTo(2);
        assertThat(contar(targetUrl, "pessoa")).isEqualTo(2);
        assertThat(contar(targetUrl, "pessoa_tipo_pessoa")).isEqualTo(3);
        assertThat(contar(targetUrl, "aluno")).isEqualTo(1);
        assertThat(contar(targetUrl, "responsavel")).isEqualTo(1);
        assertThat(contar(targetUrl, "aluno_responsavel")).isEqualTo(1);
        assertThat(contar(targetUrl, "endereco")).isEqualTo(2);
        assertThat(contar(targetUrl, "pessoa_endereco")).isEqualTo(2);
        assertThat(contar(targetUrl, "people_documento_read_model")).isEqualTo(2);
        assertThat(contar(targetUrl, "people_funcionario_read_model")).isEqualTo(1);
    }

    @Test
    void bloqueiaPessoaEnderecoQuandoOrigemTemMultiplosEnderecosPrincipaisPorPessoa() throws Exception {
        String sourceUrl = h2Url("source_" + UUID.randomUUID());
        String targetUrl = h2Url("target_" + UUID.randomUUID());
        criarSchema(sourceUrl);
        criarSchema(targetUrl);
        popularOrigem(sourceUrl);
        popularEnderecoPrincipalDuplicado(sourceUrl);

        JdbcPeopleCatalogReadModelSyncAdapter adapter = new JdbcPeopleCatalogReadModelSyncAdapter(
                new PeopleReadModelSourceProperties(sourceUrl, "sa", "", "org.h2.Driver"),
                new PeopleReadModelMigrationProperties(targetUrl, "sa", "", "org.h2.Driver", List.of()));

        var reports = adapter.synchronize(true, true, 100);

        assertThat(reports)
                .filteredOn("table", "pessoa_endereco")
                .singleElement()
                .satisfies(report -> {
                    assertThat(report.status()).isEqualTo("blocked");
                    assertThat(report.reason()).isEqualTo("address-principal-rule-violated");
                    assertThat(report.backfilledRecords()).isZero();
                    assertThat(report.divergences()).isEqualTo(1);
                });
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchema(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE escola (
                        id_escola UUID NOT NULL PRIMARY KEY,
                        nome VARCHAR(150) NOT NULL
                    )
                    """);
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
                    CREATE TABLE pessoa (
                        id_pessoa UUID NOT NULL PRIMARY KEY,
                        id_escola UUID NOT NULL,
                        escola_nome VARCHAR(150) NOT NULL,
                        nome_completo VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14),
                        rg VARCHAR(20),
                        orgao_emissor_rg VARCHAR(20),
                        uf_rg VARCHAR(2),
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        data_nascimento DATE,
                        sexo VARCHAR(20),
                        nome_social VARCHAR(150),
                        nacionalidade VARCHAR(80),
                        naturalidade VARCHAR(100),
                        ativo BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pessoa_tipo_pessoa (
                        id_pessoa_tipo_pessoa UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL REFERENCES pessoa(id_pessoa),
                        id_tipo_pessoa UUID NOT NULL REFERENCES tipo_pessoa(id_tipo_pessoa),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (id_pessoa, id_tipo_pessoa)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE aluno (
                        id_aluno UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID,
                        nome_completo VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14),
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        data_nascimento DATE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE responsavel (
                        id_responsavel UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID,
                        nome_completo VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14),
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE aluno_responsavel (
                        id_aluno_responsavel UUID NOT NULL PRIMARY KEY,
                        id_aluno UUID NOT NULL REFERENCES aluno(id_aluno),
                        id_responsavel UUID NOT NULL REFERENCES responsavel(id_responsavel),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (id_aluno, id_responsavel)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE endereco (
                        id_endereco UUID NOT NULL PRIMARY KEY,
                        cep VARCHAR(10),
                        logradouro VARCHAR(150),
                        numero VARCHAR(20),
                        complemento VARCHAR(100),
                        bairro VARCHAR(100),
                        cidade VARCHAR(100),
                        uf VARCHAR(2),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pessoa_endereco (
                        id_pessoa_endereco UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL REFERENCES pessoa(id_pessoa),
                        id_endereco UUID NOT NULL REFERENCES endereco(id_endereco),
                        id_tipo_endereco UUID REFERENCES tipo_endereco(id_tipo_endereco),
                        principal BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE documento (
                        id_documento UUID NOT NULL PRIMARY KEY,
                        id_tipo_documento UUID NOT NULL,
                        numero_documento VARCHAR(50),
                        caminho_arquivo VARCHAR(255) NOT NULL,
                        observacao VARCHAR(255),
                        data_upload TIMESTAMP,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE tipo_documento (
                        id_tipo_documento UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(50) NOT NULL,
                        descricao VARCHAR(150) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pessoa_documento (
                        id_pessoa_documento UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_documento UUID NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE people_documento_read_model (
                        id_pessoa_documento UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_documento UUID NOT NULL,
                        id_tipo_documento UUID NOT NULL,
                        tipo_documento_codigo VARCHAR(50) NOT NULL,
                        tipo_documento_descricao VARCHAR(150) NOT NULL,
                        numero_documento VARCHAR(50),
                        caminho_arquivo VARCHAR(255) NOT NULL,
                        observacao VARCHAR(255),
                        data_upload TIMESTAMP,
                        id_escola UUID NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE cargo (
                        id_cargo UUID NOT NULL PRIMARY KEY,
                        descricao VARCHAR(120) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE funcionario (
                        id_funcionario UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_cargo UUID,
                        ativo BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE people_funcionario_read_model (
                        id_funcionario UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_escola UUID NOT NULL,
                        nome_completo VARCHAR(200) NOT NULL,
                        cargo_descricao VARCHAR(120),
                        ativo BOOLEAN NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    private void popularOrigem(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO escola (id_escola, nome) VALUES
                    ('00000000-0000-0000-0000-000000000047', 'Escola Principal')
                    """);
            statement.execute("""
                    INSERT INTO tipo_pessoa (id_tipo_pessoa, codigo, descricao, created_at) VALUES
                    ('11111111-1111-1111-1111-111111111111', 'ALUNO', 'Aluno', CURRENT_TIMESTAMP),
                    ('22222222-2222-2222-2222-222222222222', 'RESPONSAVEL', 'Responsavel', CURRENT_TIMESTAMP),
                    ('33333333-3333-3333-3333-333333333333', 'PROFESSOR', 'Professor', CURRENT_TIMESTAMP)
                    """);
            statement.execute("""
                    INSERT INTO tipo_endereco (id_tipo_endereco, codigo, descricao) VALUES
                    ('44444444-4444-4444-4444-444444444444', 'RESIDENCIAL', 'Residencial'),
                    ('55555555-5555-5555-5555-555555555555', 'COMERCIAL', 'Comercial')
                    """);
            statement.execute("""
                    INSERT INTO pessoa (
                        id_pessoa, id_escola, escola_nome, nome_completo, cpf, rg, orgao_emissor_rg, uf_rg, email, telefone,
                        data_nascimento, sexo, nome_social, nacionalidade, naturalidade, ativo, created_at, updated_at
                    ) VALUES
                    (
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '00000000-0000-0000-0000-000000000047',
                        'Escola Principal',
                        'Ana Aluna',
                        '11111111111',
                        'MG123',
                        'SSP',
                        'MG',
                        'ana@example.test',
                        '31999990000',
                        DATE '2010-01-02',
                        'FEMININO',
                        NULL,
                        'Brasileira',
                        'Belo Horizonte',
                        TRUE,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    ),
                    (
                        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                        '00000000-0000-0000-0000-000000000047',
                        'Escola Principal',
                        'Rita Responsavel',
                        '22222222222',
                        NULL,
                        NULL,
                        NULL,
                        'rita@example.test',
                        '31999991111',
                        DATE '1980-03-04',
                        'FEMININO',
                        NULL,
                        'Brasileira',
                        'Contagem',
                        TRUE,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa_tipo_pessoa (id_pessoa_tipo_pessoa, id_pessoa, id_tipo_pessoa, created_at) VALUES
                    (
                        '99999999-9999-9999-9999-999999999991',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '11111111-1111-1111-1111-111111111111',
                        CURRENT_TIMESTAMP
                    ),
                    (
                        '99999999-9999-9999-9999-999999999992',
                        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                        '22222222-2222-2222-2222-222222222222',
                        CURRENT_TIMESTAMP
                    ),
                    (
                        '99999999-9999-9999-9999-999999999993',
                        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                        '33333333-3333-3333-3333-333333333333',
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO aluno (
                        id_aluno, id_pessoa, nome_completo, cpf, email, telefone, data_nascimento, created_at
                    ) VALUES (
                        'aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa',
                        NULL,
                        'Ana Aluna',
                        '11111111111',
                        'ana@example.test',
                        '31999990000',
                        DATE '2010-01-02',
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO responsavel (
                        id_responsavel, id_pessoa, nome_completo, cpf, email, telefone, created_at
                    ) VALUES (
                        'bbbbbbbb-2222-2222-2222-bbbbbbbbbbbb',
                        NULL,
                        'Rita Responsavel',
                        '22222222222',
                        'rita@example.test',
                        '31999991111',
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO aluno_responsavel (
                        id_aluno_responsavel, id_aluno, id_responsavel, created_at
                    ) VALUES (
                        'cccccccc-3333-3333-3333-cccccccccccc',
                        'aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa',
                        'bbbbbbbb-2222-2222-2222-bbbbbbbbbbbb',
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO endereco (
                        id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf, created_at, updated_at
                    ) VALUES
                    (
                        'dddddddd-4444-4444-4444-dddddddddddd',
                        '30110000',
                        'Rua Principal',
                        '100',
                        NULL,
                        'Centro',
                        'Belo Horizonte',
                        'MG',
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    ),
                    (
                        'eeeeeeee-5555-5555-5555-eeeeeeeeeeee',
                        '32220000',
                        'Avenida Responsavel',
                        '200',
                        'Casa',
                        'Industrial',
                        'Contagem',
                        'MG',
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa_endereco (
                        id_pessoa_endereco, id_pessoa, id_endereco, id_tipo_endereco, principal, created_at
                    ) VALUES
                    (
                        'abababab-1111-1111-1111-abababababab',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        'dddddddd-4444-4444-4444-dddddddddddd',
                        '44444444-4444-4444-4444-444444444444',
                        TRUE,
                        CURRENT_TIMESTAMP
                    ),
                    (
                        'bcbcbcbc-2222-2222-2222-bcbcbcbcbcbc',
                        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                        'eeeeeeee-5555-5555-5555-eeeeeeeeeeee',
                        '44444444-4444-4444-4444-444444444444',
                        TRUE,
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO tipo_documento (id_tipo_documento, codigo, descricao) VALUES
                    ('12121212-1212-1212-1212-121212121212', 'CPF', 'CPF'),
                    ('34343434-3434-3434-3434-343434343434', 'RG', 'Registro Geral')
                    """);
            statement.execute("""
                    INSERT INTO cargo (id_cargo, descricao) VALUES
                    ('45454545-4545-4545-4545-454545454545', 'Coordenadora')
                    """);
            statement.execute("""
                    INSERT INTO documento (
                        id_documento, id_tipo_documento, numero_documento, caminho_arquivo, observacao, data_upload, created_at
                    ) VALUES
                    (
                        '56565656-5656-5656-5656-565656565656',
                        '12121212-1212-1212-1212-121212121212',
                        '11111111111',
                        '/docs/cpf-ana.pdf',
                        'Frente',
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    ),
                    (
                        '78787878-7878-7878-7878-787878787878',
                        '34343434-3434-3434-3434-343434343434',
                        'MG123456',
                        '/docs/rg-rita.pdf',
                        'Verso',
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa_documento (
                        id_pessoa_documento, id_pessoa, id_documento, created_at
                    ) VALUES
                    (
                        '90909090-9090-9090-9090-909090909090',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        '56565656-5656-5656-5656-565656565656',
                        CURRENT_TIMESTAMP
                    ),
                    (
                        'a0a0a0a0-a0a0-a0a0-a0a0-a0a0a0a0a0a0',
                        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                        '78787878-7878-7878-7878-787878787878',
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO funcionario (
                        id_funcionario, id_pessoa, id_cargo, ativo, created_at
                    ) VALUES (
                        'f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1',
                        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
                        '45454545-4545-4545-4545-454545454545',
                        TRUE,
                        CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    private void popularEnderecoPrincipalDuplicado(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO endereco (
                        id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf, created_at, updated_at
                    ) VALUES (
                        'ffffffff-6666-6666-6666-ffffffffffff',
                        '30110001',
                        'Rua Duplicada',
                        '101',
                        NULL,
                        'Centro',
                        'Belo Horizonte',
                        'MG',
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa_endereco (
                        id_pessoa_endereco, id_pessoa, id_endereco, id_tipo_endereco, principal, created_at
                    ) VALUES (
                        'cdcdcdcd-3333-3333-3333-cdcdcdcdcdcd',
                        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                        'ffffffff-6666-6666-6666-ffffffffffff',
                        '44444444-4444-4444-4444-444444444444',
                        TRUE,
                        CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    private int contar(String url, String table) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}

