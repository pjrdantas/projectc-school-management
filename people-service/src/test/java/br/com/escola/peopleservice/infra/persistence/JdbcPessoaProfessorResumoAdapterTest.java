package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeoplePersistenceProperties;

class JdbcPessoaProfessorResumoAdapterTest {

    @Test
    void buscaProfessorPorIdDentroDaEscola() throws Exception {
        String url = "jdbc:h2:mem:professor_summary_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        UUID escolaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        prepararSchema(url, escolaId, professorId, pessoaId, funcionarioId);

        JdbcPessoaProfessorResumoAdapter adapter = adapter(url);

        var response = adapter.buscarProfessorPorId(professorId, escolaId);

        assertThat(response).isPresent();
        assertThat(response.orElseThrow().nomeCompleto()).isEqualTo("Carla Mendes");
        assertThat(response.orElseThrow().funcionarioId()).isEqualTo(funcionarioId);
        assertThat(response.orElseThrow().ativo()).isTrue();
    }

    @Test
    void listaApenasProfessoresAtivosDaEscolaOrdenadosPorNome() throws Exception {
        String url = "jdbc:h2:mem:professor_summary_list_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        UUID escolaId = UUID.randomUUID();
        prepararSchema(url, escolaId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO people_professor_read_model (
                        id_professor, id_pessoa, id_funcionario, id_escola, nome_completo, ativo, created_at
                    ) VALUES (
                        RANDOM_UUID(), RANDOM_UUID(), RANDOM_UUID(), RANDOM_UUID(), 'Outro Escola', TRUE, CURRENT_TIMESTAMP
                    ), (
                        RANDOM_UUID(), RANDOM_UUID(), RANDOM_UUID(), '%s', 'Bruno Lima', FALSE, CURRENT_TIMESTAMP
                    ), (
                        RANDOM_UUID(), RANDOM_UUID(), RANDOM_UUID(), '%s', 'Daniela Alves', TRUE, CURRENT_TIMESTAMP
                    )
                    """.formatted(escolaId, escolaId));
        }

        JdbcPessoaProfessorResumoAdapter adapter = adapter(url);

        List<?> response = adapter.listarProfessoresPorEscola(escolaId);

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting("nomeCompleto")
                .containsExactly("Carla Mendes", "Daniela Alves");
    }

    @Test
    void falhaQuandoUrlNaoFoiConfigurada() {
        JdbcPessoaProfessorResumoAdapter adapter = new JdbcPessoaProfessorResumoAdapter(
                new PeoplePersistenceProperties("", "sa", "", "org.h2.Driver", List.of()));

        assertThatThrownBy(() -> adapter.listarProfessoresPorEscola(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("professor-local-read-url-required");
    }

    private JdbcPessoaProfessorResumoAdapter adapter(String url) {
        return new JdbcPessoaProfessorResumoAdapter(
                new PeoplePersistenceProperties(
                        url,
                        "sa",
                        "",
                        "org.h2.Driver",
                        List.of("classpath:db/people/migration")));
    }

    private void prepararSchema(String url, UUID escolaId, UUID professorId, UUID pessoaId, UUID funcionarioId) throws Exception {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE people_professor_read_model (
                        id_professor UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_funcionario UUID,
                        id_escola UUID NOT NULL,
                        nome_completo VARCHAR(200) NOT NULL,
                        ativo BOOLEAN NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO people_professor_read_model (
                        id_professor, id_pessoa, id_funcionario, id_escola, nome_completo, ativo, created_at
                    ) VALUES (
                        '%s', '%s', '%s', '%s', 'Carla Mendes', TRUE, CURRENT_TIMESTAMP
                    )
                    """.formatted(professorId, pessoaId, funcionarioId, escolaId));
        }
    }
}

