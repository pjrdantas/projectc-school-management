package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeoplePersistenceProperties;

class JdbcPessoaFuncionarioResumoAdapterTest {

    @Test
    void buscaFuncionarioPorIdDentroDaEscola() throws Exception {
        String url = "jdbc:h2:mem:funcionario_summary_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        UUID escolaId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        prepararSchema(url, escolaId, funcionarioId, pessoaId);

        JdbcPessoaFuncionarioResumoAdapter adapter = adapter(url);

        var response = adapter.buscarFuncionarioPorId(funcionarioId, escolaId);

        assertThat(response).isPresent();
        assertThat(response.orElseThrow().nomeCompleto()).isEqualTo("Ana Souza");
        assertThat(response.orElseThrow().cargoDescricao()).isEqualTo("Coordenadora");
        assertThat(response.orElseThrow().ativo()).isTrue();
    }

    @Test
    void listaApenasFuncionariosAtivosDaEscolaOrdenadosPorNome() throws Exception {
        String url = "jdbc:h2:mem:funcionario_summary_list_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        UUID escolaId = UUID.randomUUID();
        prepararSchema(url, escolaId, UUID.randomUUID(), UUID.randomUUID());
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO people_funcionario_read_model (
                        id_funcionario, id_pessoa, id_escola, nome_completo, cargo_descricao, ativo, created_at
                    ) VALUES (
                        RANDOM_UUID(), RANDOM_UUID(), RANDOM_UUID(), 'Outro Escola', 'Diretor', TRUE, CURRENT_TIMESTAMP
                    ), (
                        RANDOM_UUID(), RANDOM_UUID(), '%s', 'Bruno Lima', 'Professor', FALSE, CURRENT_TIMESTAMP
                    ), (
                        RANDOM_UUID(), RANDOM_UUID(), '%s', 'Carla Dias', 'Secretaria', TRUE, CURRENT_TIMESTAMP
                    )
                    """.formatted(escolaId, escolaId));
        }

        JdbcPessoaFuncionarioResumoAdapter adapter = adapter(url);

        List<?> response = adapter.listarFuncionariosAtivosPorEscola(escolaId);

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting("nomeCompleto")
                .containsExactly("Ana Souza", "Carla Dias");
    }

    @Test
    void falhaQuandoUrlNaoFoiConfigurada() {
        JdbcPessoaFuncionarioResumoAdapter adapter = new JdbcPessoaFuncionarioResumoAdapter(
                new PeoplePersistenceProperties("", "sa", "", "org.h2.Driver", List.of()));

        assertThatThrownBy(() -> adapter.listarFuncionariosAtivosPorEscola(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("funcionario-internal-summary-local-read-url-required");
    }

    private JdbcPessoaFuncionarioResumoAdapter adapter(String url) {
        return new JdbcPessoaFuncionarioResumoAdapter(
                new PeoplePersistenceProperties(
                        url,
                        "sa",
                        "",
                        "org.h2.Driver",
                        List.of("classpath:db/people/migration")));
    }

    private void prepararSchema(String url, UUID escolaId, UUID funcionarioId, UUID pessoaId) throws Exception {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE people_funcionario_read_model (
                        id_funcionario UUID NOT NULL PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        id_escola UUID NOT NULL,
                        nome_completo VARCHAR(200) NOT NULL,
                        cargo_descricao VARCHAR(120),
                        ativo BOOLEAN NOT NULL,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO people_funcionario_read_model (
                        id_funcionario, id_pessoa, id_escola, nome_completo, cargo_descricao, ativo, created_at
                    ) VALUES (
                        '%s', '%s', '%s', 'Ana Souza', 'Coordenadora', TRUE, CURRENT_TIMESTAMP
                    )
                    """.formatted(funcionarioId, pessoaId, escolaId));
        }
    }
}


