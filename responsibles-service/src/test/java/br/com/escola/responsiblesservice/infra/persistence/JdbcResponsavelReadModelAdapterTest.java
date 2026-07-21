package br.com.escola.responsiblesservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.dto.ResponsavelAlunoVinculadoReadModelResponse;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesPersistenceProperties;

class JdbcResponsavelReadModelAdapterTest {

    @Test
    void deveListarResponsaveisDoReadModelLocalComFiltrosMinimos() throws Exception {
        String url = h2Url("responsibles_read_list_" + UUID.randomUUID());
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        criarSchemaEPopular(url);
        JdbcResponsavelReadModelAdapter adapter = new JdbcResponsavelReadModelAdapter(
                new ResponsiblesPersistenceProperties(url, "sa", "", "org.h2.Driver"));

        var response = adapter.listarResponsaveis(escolaId, "Maria", "98765432100");

        assertThat(response).isPresent();
        assertThat(response.get()).hasSize(1);
        assertThat(response.get().getFirst().nomeCompleto()).isEqualTo("Maria Souza");
        assertThat(response.get().getFirst().escolaNome()).isEqualTo("Escola padrao");
    }

    @Test
    void deveBuscarResponsavelPorIdNoReadModelLocal() throws Exception {
        String url = h2Url("responsibles_read_detail_" + UUID.randomUUID());
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        criarSchemaEPopular(url);
        JdbcResponsavelReadModelAdapter adapter = new JdbcResponsavelReadModelAdapter(
                new ResponsiblesPersistenceProperties(url, "sa", "", "org.h2.Driver"));

        var response = adapter.buscarResponsavelPorId(responsavelId, escolaId);

        assertThat(response).isPresent();
        assertThat(response.get().nomeCompleto()).isEqualTo("Maria Souza");
        assertThat(response.get().cpf()).isEqualTo("98765432100");
    }

    @Test
    void deveListarResponsaveisPorAlunoComCamposDoVinculoNoReadModelLocal() throws Exception {
        String url = h2Url("responsibles_read_student_link_" + UUID.randomUUID());
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000401");
        criarSchemaEPopular(url);
        JdbcResponsavelReadModelAdapter adapter = new JdbcResponsavelReadModelAdapter(
                new ResponsiblesPersistenceProperties(url, "sa", "", "org.h2.Driver"));

        Optional<List<ResponsavelAlunoVinculadoReadModelResponse>> response = adapter.listarResponsaveisPorAluno(alunoId,
                escolaId);

        assertThat(response).isPresent();
        assertThat(response.orElseThrow()).singleElement().satisfies(responsavel -> {
            assertThat(responsavel.nomeCompleto()).isEqualTo("Maria Souza");
            assertThat(responsavel.parentesco()).isEqualTo("MAE");
            assertThat(responsavel.responsavelFinanceiro()).isTrue();
            assertThat(responsavel.responsavelPedagogico()).isFalse();
            assertThat(responsavel.autorizadoRetirar()).isTrue();
        });
    }

    @Test
    void deveFazerFallbackQuandoAlunoNaoTemVinculoNoReadModelLocal() throws Exception {
        String url = h2Url("responsibles_read_student_link_missing_" + UUID.randomUUID());
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        criarSchemaEPopular(url);
        JdbcResponsavelReadModelAdapter adapter = new JdbcResponsavelReadModelAdapter(
                new ResponsiblesPersistenceProperties(url, "sa", "", "org.h2.Driver"));

        var response = adapter.listarResponsaveisPorAluno(
                UUID.fromString("00000000-0000-0000-0000-000000000499"),
                escolaId);

        assertThat(response).isEmpty();
    }

    private String h2Url(String dbName) {
        return "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }

    private void criarSchemaEPopular(String url) throws SQLException {
        try (var connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE responsavel (
                        id_responsavel UUID NOT NULL PRIMARY KEY,
                        nome_completo VARCHAR(150) NOT NULL,
                        cpf VARCHAR(14),
                        email VARCHAR(150),
                        telefone VARCHAR(20),
                        rg VARCHAR(20),
                        cep VARCHAR(14),
                        logradouro VARCHAR(200),
                        numero VARCHAR(20),
                        complemento VARCHAR(120),
                        bairro VARCHAR(120),
                        cidade VARCHAR(120),
                        uf VARCHAR(2),
                        id_escola UUID NOT NULL,
                        escola_nome VARCHAR(150),
                        ativo BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE parentesco (
                        id_parentesco UUID NOT NULL PRIMARY KEY,
                        codigo VARCHAR(40) NOT NULL,
                        descricao VARCHAR(120) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE aluno_responsavel (
                        id_aluno_responsavel UUID NOT NULL PRIMARY KEY,
                        id_aluno UUID NOT NULL,
                        id_responsavel UUID NOT NULL,
                        id_parentesco UUID,
                        responsavel_financeiro BOOLEAN NOT NULL DEFAULT FALSE,
                        responsavel_pedagogico BOOLEAN NOT NULL DEFAULT FALSE,
                        autorizado_retirar BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at TIMESTAMP NOT NULL,
                        CONSTRAINT fk_parentesco
                            FOREIGN KEY (id_parentesco) REFERENCES parentesco(id_parentesco),
                        CONSTRAINT fk_responsavel
                            FOREIGN KEY (id_responsavel) REFERENCES responsavel(id_responsavel)
                    )
                    """);
            statement.execute("""
                    INSERT INTO responsavel (
                        id_responsavel, nome_completo, cpf, email, telefone, rg,
                        cep, logradouro, numero, complemento, bairro, cidade, uf,
                        id_escola, escola_nome, created_at
                    ) VALUES (
                        '00000000-0000-0000-0000-000000000601',
                        'Maria Souza',
                        '98765432100',
                        'maria.souza@example.com',
                        '11988887777',
                        '1234567',
                        '01001000',
                        'Rua Central',
                        '100',
                        'Casa',
                        'Centro',
                        'Sao Paulo',
                        'SP',
                        '00000000-0000-0000-0000-000000000047',
                        'Escola padrao',
                        TIMESTAMP '2026-07-16 10:00:00'
                    )
                    """);
            statement.execute("""
                    INSERT INTO parentesco (id_parentesco, codigo, descricao) VALUES (
                        '00000000-0000-0000-0000-000000000301',
                        'MAE',
                        'Mae'
                    )
                    """);
            statement.execute("""
                    INSERT INTO aluno_responsavel (
                        id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                        responsavel_financeiro, responsavel_pedagogico, autorizado_retirar, created_at
                    ) VALUES (
                        '00000000-0000-0000-0000-000000000501',
                        '00000000-0000-0000-0000-000000000401',
                        '00000000-0000-0000-0000-000000000601',
                        '00000000-0000-0000-0000-000000000301',
                        TRUE,
                        FALSE,
                        TRUE,
                        TIMESTAMP '2026-07-16 10:00:00'
                    )
                    """);
        }
    }
}

