package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculadoResponse;
import br.com.escola.peopleservice.infra.config.PersistenceProperties;

class JdbcAlunoResponsavelAdapterTest {

    @Test
    void bloqueiaLeituraLocalQuandoUrlNaoEstaConfigurada() {
        JdbcAlunoResponsavelAdapter adapter = new JdbcAlunoResponsavelAdapter(
                new PersistenceProperties("", "", "", "", List.of()));

        assertThatThrownBy(() -> adapter.consultarCadastro(null, null, null, null, 0, 20))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("student-responsible-local-read-url-required");
    }

    @Test
    void consultaCadastroLocalComFiltrosPaginacaoOrdenacaoEAgregacao() throws Exception {
        String url = h2Url("student_responsible_" + UUID.randomUUID());
        criarSchema(url);
        popularDados(url);
        JdbcAlunoResponsavelAdapter adapter = new JdbcAlunoResponsavelAdapter(
                new PersistenceProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var page = adapter.consultarCadastro("Ana", null, "Rita", null, 0, 10);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(10);
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().idAluno())
                .isEqualTo(UUID.fromString("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa"));
        assertThat(page.content().getFirst().nomeCompleto()).isEqualTo("Ana Aluna");
        assertThat(page.content().getFirst().responsaveis())
                .extracting("nomeCompleto")
                .containsExactly("Rita Responsavel");
    }

    @Test
    void limitaTamanhoDaPaginaENormalizaFiltrosEmBranco() throws Exception {
        String url = h2Url("student_responsible_page_" + UUID.randomUUID());
        criarSchema(url);
        popularDados(url);
        JdbcAlunoResponsavelAdapter adapter = new JdbcAlunoResponsavelAdapter(
                new PersistenceProperties(url, "sa", "", "org.h2.Driver", List.of()));

        var page = adapter.consultarCadastro(" ", " ", " ", " ", -1, 200);

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(100);
        assertThat(page.content())
                .extracting("nomeCompleto")
                .containsExactly("Ana Aluna", "Bruno Aluno");
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchema(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
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
                    CREATE TABLE pessoa (
                        id_pessoa UUID NOT NULL PRIMARY KEY,
                        rg VARCHAR(20)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE parentesco (
                        id_parentesco UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL
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
                        uf VARCHAR(2)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pessoa_endereco (
                        id_pessoa_endereco UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_endereco UUID NOT NULL,
                        principal BOOLEAN NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE aluno_responsavel (
                        id_aluno_responsavel UUID NOT NULL PRIMARY KEY,
                        id_aluno UUID NOT NULL REFERENCES aluno(id_aluno),
                        id_responsavel UUID NOT NULL REFERENCES responsavel(id_responsavel),
                        id_parentesco UUID REFERENCES parentesco(id_parentesco),
                        responsavel_financeiro BOOLEAN NOT NULL DEFAULT FALSE,
                        responsavel_pedagogico BOOLEAN NOT NULL DEFAULT FALSE,
                        autorizado_retirar BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (id_aluno, id_responsavel)
                    )
                    """);
        }
    }

    private void popularDados(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO aluno (
                        id_aluno, id_pessoa, nome_completo, cpf, email, telefone, data_nascimento, created_at
                    ) VALUES
                    (
                        'aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa',
                        NULL,
                        'Ana Aluna',
                        '11111111111',
                        'ana@example.test',
                        '31999990000',
                        DATE '2010-01-02',
                        TIMESTAMP '2026-01-01 10:00:00'
                    ),
                    (
                        'bbbbbbbb-1111-1111-1111-bbbbbbbbbbbb',
                        NULL,
                        'Bruno Aluno',
                        '33333333333',
                        'bruno@example.test',
                        '31999992222',
                        DATE '2011-02-03',
                        TIMESTAMP '2026-01-02 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa (id_pessoa, rg) VALUES
                    (
                        '11111111-aaaa-aaaa-aaaa-111111111111',
                        'MG123456'
                    ),
                    (
                        '22222222-bbbb-bbbb-bbbb-222222222222',
                        'SP654321'
                    )
                    """);
            statement.execute("""
                    INSERT INTO parentesco (id_parentesco, codigo) VALUES
                    (
                        'aaaaaaaa-9999-9999-9999-aaaaaaaaaaaa',
                        'MAE'
                    ),
                    (
                        'bbbbbbbb-9999-9999-9999-bbbbbbbbbbbb',
                        'PAI'
                    )
                    """);
            statement.execute("""
                    INSERT INTO endereco (
                        id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf
                    ) VALUES
                    (
                        'aaaaaaaa-eeee-eeee-eeee-aaaaaaaaaaaa',
                        '30110000',
                        'Rua Um',
                        '100',
                        'Casa',
                        'Centro',
                        'Belo Horizonte',
                        'MG'
                    ),
                    (
                        'bbbbbbbb-eeee-eeee-eeee-bbbbbbbbbbbb',
                        '01310000',
                        'Rua Dois',
                        '200',
                        NULL,
                        'Bela Vista',
                        'Sao Paulo',
                        'SP'
                    )
                    """);
            statement.execute("""
                    INSERT INTO pessoa_endereco (
                        id_pessoa_endereco, id_pessoa, id_endereco, principal
                    ) VALUES
                    (
                        'aaaaaaaa-dddd-dddd-dddd-aaaaaaaaaaaa',
                        '11111111-aaaa-aaaa-aaaa-111111111111',
                        'aaaaaaaa-eeee-eeee-eeee-aaaaaaaaaaaa',
                        TRUE
                    ),
                    (
                        'bbbbbbbb-dddd-dddd-dddd-bbbbbbbbbbbb',
                        '22222222-bbbb-bbbb-bbbb-222222222222',
                        'bbbbbbbb-eeee-eeee-eeee-bbbbbbbbbbbb',
                        TRUE
                    )
                    """);
            statement.execute("""
                    INSERT INTO responsavel (
                        id_responsavel, id_pessoa, nome_completo, cpf, email, telefone, created_at
                    ) VALUES
                    (
                        'cccccccc-2222-2222-2222-cccccccccccc',
                        '11111111-aaaa-aaaa-aaaa-111111111111',
                        'Rita Responsavel',
                        '22222222222',
                        'rita@example.test',
                        '31999991111',
                        TIMESTAMP '2026-01-03 10:00:00'
                    ),
                    (
                        'dddddddd-2222-2222-2222-dddddddddddd',
                        '22222222-bbbb-bbbb-bbbb-222222222222',
                        'Carlos Responsavel',
                        '44444444444',
                        'carlos@example.test',
                        '31999993333',
                        TIMESTAMP '2026-01-04 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO aluno_responsavel (
                        id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                        responsavel_financeiro, responsavel_pedagogico, autorizado_retirar, created_at
                    ) VALUES
                    (
                        'eeeeeeee-3333-3333-3333-eeeeeeeeeeee',
                        'aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa',
                        'cccccccc-2222-2222-2222-cccccccccccc',
                        'aaaaaaaa-9999-9999-9999-aaaaaaaaaaaa',
                        TRUE,
                        TRUE,
                        FALSE,
                        TIMESTAMP '2026-01-05 10:00:00'
                    ),
                    (
                        'ffffffff-3333-3333-3333-ffffffffffff',
                        'bbbbbbbb-1111-1111-1111-bbbbbbbbbbbb',
                        'dddddddd-2222-2222-2222-dddddddddddd',
                        'bbbbbbbb-9999-9999-9999-bbbbbbbbbbbb',
                        FALSE,
                        TRUE,
                        TRUE,
                        TIMESTAMP '2026-01-06 10:00:00'
                    )
                    """);
        }
    }

    @Test
    void listaResponsaveisPorAlunoComCamposDoVinculoEDetalhe() throws Exception {
        String url = h2Url("student_responsible_detail_" + UUID.randomUUID());
        criarSchema(url);
        popularDados(url);
        JdbcAlunoResponsavelAdapter adapter = new JdbcAlunoResponsavelAdapter(
                new PersistenceProperties(url, "sa", "", "org.h2.Driver", List.of()));

        Optional<List<PessoaResponsavelVinculadoResponse>> response = adapter.listarResponsaveisPorAluno(
                UUID.fromString("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa"));

        assertThat(response).isPresent();
        assertThat(response.orElseThrow()).singleElement().satisfies(responsavel -> {
            assertThat(responsavel.nomeCompleto()).isEqualTo("Rita Responsavel");
            assertThat(responsavel.parentesco()).isEqualTo("MAE");
            assertThat(responsavel.rg()).isEqualTo("MG123456");
            assertThat(responsavel.cep()).isEqualTo("30110000");
            assertThat(responsavel.responsavelFinanceiro()).isTrue();
            assertThat(responsavel.responsavelPedagogico()).isTrue();
            assertThat(responsavel.autorizadoRetirar()).isFalse();
        });
    }

    @Test
    void retornaVazioQuandoAlunoNaoExisteNoReadModelLocal() throws Exception {
        String url = h2Url("student_responsible_missing_" + UUID.randomUUID());
        criarSchema(url);
        popularDados(url);
        JdbcAlunoResponsavelAdapter adapter = new JdbcAlunoResponsavelAdapter(
                new PersistenceProperties(url, "sa", "", "org.h2.Driver", List.of()));

        Optional<List<PessoaResponsavelVinculadoResponse>> response = adapter.listarResponsaveisPorAluno(UUID.randomUUID());

        assertThat(response).isEmpty();
    }
}


