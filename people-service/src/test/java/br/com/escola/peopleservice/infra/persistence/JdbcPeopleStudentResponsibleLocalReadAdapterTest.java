package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleLocalReadModelSchemaMigrationProperties;

class JdbcPeopleStudentResponsibleLocalReadAdapterTest {

    @Test
    void bloqueiaLeituraLocalQuandoUrlNaoEstaConfigurada() {
        JdbcPeopleStudentResponsibleLocalReadAdapter adapter = new JdbcPeopleStudentResponsibleLocalReadAdapter(
                new PeopleLocalReadModelSchemaMigrationProperties("", "", "", "", List.of()));

        assertThatThrownBy(() -> adapter.consultarCadastro(null, null, null, null, 0, 20))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("student-responsible-local-read-url-required");
    }

    @Test
    void consultaCadastroLocalComFiltrosPaginacaoOrdenacaoEAgregacao() throws Exception {
        String url = h2Url("student_responsible_" + UUID.randomUUID());
        criarSchema(url);
        popularDados(url);
        JdbcPeopleStudentResponsibleLocalReadAdapter adapter = new JdbcPeopleStudentResponsibleLocalReadAdapter(
                new PeopleLocalReadModelSchemaMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

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
        JdbcPeopleStudentResponsibleLocalReadAdapter adapter = new JdbcPeopleStudentResponsibleLocalReadAdapter(
                new PeopleLocalReadModelSchemaMigrationProperties(url, "sa", "", "org.h2.Driver", List.of()));

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
                    CREATE TABLE aluno_responsavel (
                        id_aluno_responsavel UUID NOT NULL PRIMARY KEY,
                        id_aluno UUID NOT NULL REFERENCES aluno(id_aluno),
                        id_responsavel UUID NOT NULL REFERENCES responsavel(id_responsavel),
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
                    INSERT INTO responsavel (
                        id_responsavel, id_pessoa, nome_completo, cpf, email, telefone, created_at
                    ) VALUES
                    (
                        'cccccccc-2222-2222-2222-cccccccccccc',
                        NULL,
                        'Rita Responsavel',
                        '22222222222',
                        'rita@example.test',
                        '31999991111',
                        TIMESTAMP '2026-01-03 10:00:00'
                    ),
                    (
                        'dddddddd-2222-2222-2222-dddddddddddd',
                        NULL,
                        'Carlos Responsavel',
                        '44444444444',
                        'carlos@example.test',
                        '31999993333',
                        TIMESTAMP '2026-01-04 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO aluno_responsavel (
                        id_aluno_responsavel, id_aluno, id_responsavel, created_at
                    ) VALUES
                    (
                        'eeeeeeee-3333-3333-3333-eeeeeeeeeeee',
                        'aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa',
                        'cccccccc-2222-2222-2222-cccccccccccc',
                        TIMESTAMP '2026-01-05 10:00:00'
                    ),
                    (
                        'ffffffff-3333-3333-3333-ffffffffffff',
                        'bbbbbbbb-1111-1111-1111-bbbbbbbbbbbb',
                        'dddddddd-2222-2222-2222-dddddddddddd',
                        TIMESTAMP '2026-01-06 10:00:00'
                    )
                    """);
        }
    }
}
