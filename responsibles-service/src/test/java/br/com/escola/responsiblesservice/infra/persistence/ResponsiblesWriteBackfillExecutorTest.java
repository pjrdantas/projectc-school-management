package br.com.escola.responsiblesservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class ResponsiblesWriteBackfillExecutorTest {

    @Test
    void deveCopiarResponsaveisVinculosECatalogoDeFormaIdempotenteEPreservarRegistrosLocais() {
        JdbcTemplate source = sourceDatabase();
        JdbcTemplate target = targetDatabase();
        UUID escolaId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID parentescoId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 7, 21, 13, 0);

        source.update("INSERT INTO escola (id_escola, nome) VALUES (?, ?)", escolaId, "Escola origem");
        source.update("""
                INSERT INTO pessoa (id_pessoa, id_escola, nome_completo, cpf, email, telefone, rg, ativo, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, pessoaId, escolaId, "Maria Importada", "12345678901", "maria@example.com", "11999999999",
                "RG-1", true, now, now);
        source.update("INSERT INTO responsavel (id_responsavel, id_pessoa, created_at) VALUES (?, ?, ?)",
                responsavelId, pessoaId, now);
        source.update("""
                INSERT INTO endereco (id_endereco, cep, logradouro, numero, complemento, bairro, cidade, uf)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, enderecoId, "01001000", "Rua Central", "10", "Casa", "Centro", "Sao Paulo", "SP");
        source.update("""
                INSERT INTO pessoa_endereco (id_pessoa_endereco, id_pessoa, id_endereco, principal)
                VALUES (?, ?, ?, ?)
                """, UUID.randomUUID(), pessoaId, enderecoId, true);
        source.update("INSERT INTO parentesco (id_parentesco, codigo, descricao) VALUES (?, ?, ?)",
                parentescoId, "MAE", "Mae");
        source.update("""
                INSERT INTO aluno_responsavel (
                    id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                    responsavel_financeiro, responsavel_pedagogico, autorizado_retirar, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), alunoId, responsavelId, parentescoId, true, false, true, now);

        ResponsiblesWriteBackfillExecutor executor = new ResponsiblesWriteBackfillExecutor(source, target, 1);
        ResponsiblesWriteBackfillReport first = executor.execute();
        target.update("""
                INSERT INTO responsavel (id_responsavel, nome_completo, cpf, id_escola, ativo, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), "Local Posterior", "10987654321", escolaId, true, now);
        ResponsiblesWriteBackfillReport second = executor.execute();

        assertThat(first.reconciled()).isTrue();
        assertThat(second.reconciled()).isTrue();
        assertThat(first.copiedRows())
                .containsEntry("responsavel", 1)
                .containsEntry("parentesco", 1)
                .containsEntry("aluno_responsavel", 1);
        assertThat(second.sourceRows()).isEqualTo(second.reconciledRows());
        assertThat(target.queryForObject("SELECT escola_nome FROM responsavel WHERE id_responsavel = ?", String.class,
                responsavelId)).isEqualTo("Escola origem");
        assertThat(target.queryForObject("SELECT cep FROM responsavel WHERE id_responsavel = ?", String.class,
                responsavelId)).isEqualTo("01001000");
        assertThat(target.queryForObject("""
                SELECT p.codigo FROM aluno_responsavel ar
                JOIN parentesco p ON p.id_parentesco = ar.id_parentesco
                WHERE ar.id_aluno = ? AND ar.id_responsavel = ?
                """, String.class, alunoId, responsavelId)).isEqualTo("MAE");
        assertThat(target.queryForObject("SELECT COUNT(1) FROM responsavel", Integer.class)).isEqualTo(2);
    }

    private JdbcTemplate sourceDatabase() {
        JdbcTemplate jdbc = jdbc("jdbc:h2:mem:responsibles-backfill-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        jdbc.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150) NOT NULL)");
        jdbc.execute("""
                CREATE TABLE pessoa (
                    id_pessoa UUID PRIMARY KEY, id_escola UUID NOT NULL, nome_completo VARCHAR(150) NOT NULL,
                    cpf VARCHAR(14), email VARCHAR(150), telefone VARCHAR(20), rg VARCHAR(20), ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP
                )
                """);
        jdbc.execute("CREATE TABLE responsavel (id_responsavel UUID PRIMARY KEY, id_pessoa UUID NOT NULL, created_at TIMESTAMP NOT NULL)");
        jdbc.execute("""
                CREATE TABLE endereco (
                    id_endereco UUID PRIMARY KEY, cep VARCHAR(14), logradouro VARCHAR(200), numero VARCHAR(20),
                    complemento VARCHAR(120), bairro VARCHAR(120), cidade VARCHAR(120), uf VARCHAR(2)
                )
                """);
        jdbc.execute("""
                CREATE TABLE pessoa_endereco (
                    id_pessoa_endereco UUID PRIMARY KEY, id_pessoa UUID NOT NULL, id_endereco UUID NOT NULL, principal BOOLEAN NOT NULL
                )
                """);
        jdbc.execute("CREATE TABLE parentesco (id_parentesco UUID PRIMARY KEY, codigo VARCHAR(40), descricao VARCHAR(120))");
        jdbc.execute("""
                CREATE TABLE aluno_responsavel (
                    id_aluno_responsavel UUID PRIMARY KEY, id_aluno UUID NOT NULL, id_responsavel UUID NOT NULL,
                    id_parentesco UUID, responsavel_financeiro BOOLEAN, responsavel_pedagogico BOOLEAN,
                    autorizado_retirar BOOLEAN, created_at TIMESTAMP NOT NULL
                )
                """);
        return jdbc;
    }

    private JdbcTemplate targetDatabase() {
        String url = "jdbc:h2:mem:responsibles-backfill-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "")
                .locations("classpath:db/responsibles/migration")
                .load().migrate();
        return jdbc(url);
    }

    private JdbcTemplate jdbc(String url) {
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }
}
