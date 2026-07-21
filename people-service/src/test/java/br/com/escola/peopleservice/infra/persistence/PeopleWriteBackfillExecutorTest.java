package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class PeopleWriteBackfillExecutorTest {

    @Test
    void deveCopiarSomenteAgregadoDeAlunoEReconciliarDeFormaIdempotente() {
        JdbcTemplate source = sourceDatabase();
        JdbcTemplate target = targetDatabase();
        UUID escolaId = UUID.randomUUID();
        UUID alunoPessoaId = UUID.randomUUID();
        UUID outroPessoaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID tipoPessoaId = UUID.randomUUID();
        UUID tipoEnderecoId = UUID.randomUUID();
        UUID statusAlunoId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 7, 21, 12, 0);

        source.update("INSERT INTO escola (id_escola, nome) VALUES (?, ?)", escolaId, "Escola origem");
        source.update("""
                INSERT INTO tipo_pessoa (id_tipo_pessoa, codigo, descricao, created_at)
                VALUES (?, ?, ?, ?)
                """, tipoPessoaId, "ALUNO", "Aluno", now);
        source.update("INSERT INTO tipo_endereco (id_tipo_endereco, codigo, descricao) VALUES (?, ?, ?)",
                tipoEnderecoId, "RESIDENCIAL", "Residencial");
        source.update("INSERT INTO status_aluno (id_status_aluno, codigo, descricao) VALUES (?, ?, ?)",
                statusAlunoId, "ATIVO", "Ativo");
        source.update("""
                INSERT INTO pessoa (
                    id_pessoa, id_escola, nome_completo, cpf, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, alunoPessoaId, escolaId, "Aluno Importado", "11111111111", true, now);
        source.update("""
                INSERT INTO pessoa (
                    id_pessoa, id_escola, nome_completo, cpf, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, outroPessoaId, escolaId, "Pessoa Fora do Recorte", "22222222222", true, now);
        source.update("""
                INSERT INTO pessoa_tipo_pessoa (id_pessoa_tipo_pessoa, id_pessoa, id_tipo_pessoa, created_at)
                VALUES (?, ?, ?, ?)
                """, UUID.randomUUID(), alunoPessoaId, tipoPessoaId, now);
        source.update("""
                INSERT INTO endereco (id_endereco, cep, cidade, uf, created_at)
                VALUES (?, ?, ?, ?, ?)
                """, enderecoId, "01001000", "Sao Paulo", "SP", now);
        source.update("""
                INSERT INTO pessoa_endereco (
                    id_pessoa_endereco, id_pessoa, id_endereco, id_tipo_endereco, principal, created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), alunoPessoaId, enderecoId, tipoEnderecoId, true, now);
        source.update("""
                INSERT INTO aluno (
                    id_aluno, id_pessoa, id_status_aluno, ra, rm, emancipado, data_ingresso,
                    ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, alunoId, alunoPessoaId, statusAlunoId, "RA-10", "RM-10", false,
                LocalDate.of(2026, 2, 1), true, now);

        PeopleWriteBackfillExecutor executor = new PeopleWriteBackfillExecutor(source, target, 1);
        PeopleWriteBackfillReport first = executor.execute();
        PeopleWriteBackfillReport second = executor.execute();

        assertThat(first.reconciled()).isTrue();
        assertThat(second.reconciled()).isTrue();
        assertThat(first.copiedRows())
                .containsEntry("pessoa", 1)
                .containsEntry("aluno", 1)
                .containsEntry("endereco", 1);
        assertThat(second.sourceRows()).isEqualTo(second.reconciledRows());
        assertThat(target.queryForObject("SELECT escola_nome FROM pessoa WHERE id_pessoa = ?", String.class,
                alunoPessoaId)).isEqualTo("Escola origem");
        assertThat(target.queryForObject("SELECT id_escola FROM aluno WHERE id_aluno = ?", UUID.class, alunoId))
                .isEqualTo(escolaId);
        assertThat(target.queryForObject("SELECT COUNT(1) FROM pessoa", Integer.class)).isEqualTo(1);
        assertThat(target.queryForObject("SELECT COUNT(1) FROM pessoa WHERE id_pessoa = ?", Integer.class,
                outroPessoaId)).isZero();
    }

    private JdbcTemplate sourceDatabase() {
        String url = "jdbc:h2:mem:people-backfill-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
        jdbc.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150) NOT NULL)");
        jdbc.execute("""
                CREATE TABLE tipo_pessoa (
                    id_tipo_pessoa UUID PRIMARY KEY, codigo VARCHAR(40), descricao VARCHAR(120), created_at TIMESTAMP
                )
                """);
        jdbc.execute("""
                CREATE TABLE tipo_endereco (
                    id_tipo_endereco UUID PRIMARY KEY, codigo VARCHAR(40), descricao VARCHAR(120)
                )
                """);
        jdbc.execute("""
                CREATE TABLE status_aluno (
                    id_status_aluno UUID PRIMARY KEY, codigo VARCHAR(40), descricao VARCHAR(120)
                )
                """);
        jdbc.execute("""
                CREATE TABLE pessoa (
                    id_pessoa UUID PRIMARY KEY, id_escola UUID, nome_completo VARCHAR(150), cpf VARCHAR(14),
                    rg VARCHAR(20), orgao_emissor_rg VARCHAR(20), uf_rg VARCHAR(2), email VARCHAR(150),
                    telefone VARCHAR(20), data_nascimento DATE, sexo VARCHAR(20), nome_social VARCHAR(150),
                    nacionalidade VARCHAR(80), naturalidade VARCHAR(100), ativo BOOLEAN, created_at TIMESTAMP,
                    updated_at TIMESTAMP
                )
                """);
        jdbc.execute("""
                CREATE TABLE pessoa_tipo_pessoa (
                    id_pessoa_tipo_pessoa UUID PRIMARY KEY, id_pessoa UUID, id_tipo_pessoa UUID, created_at TIMESTAMP
                )
                """);
        jdbc.execute("""
                CREATE TABLE endereco (
                    id_endereco UUID PRIMARY KEY, cep VARCHAR(10), logradouro VARCHAR(150), numero VARCHAR(20),
                    complemento VARCHAR(100), bairro VARCHAR(100), cidade VARCHAR(100), uf VARCHAR(2),
                    created_at TIMESTAMP, updated_at TIMESTAMP
                )
                """);
        jdbc.execute("""
                CREATE TABLE pessoa_endereco (
                    id_pessoa_endereco UUID PRIMARY KEY, id_pessoa UUID, id_endereco UUID, id_tipo_endereco UUID,
                    principal BOOLEAN, created_at TIMESTAMP
                )
                """);
        jdbc.execute("""
                CREATE TABLE aluno (
                    id_aluno UUID PRIMARY KEY, id_pessoa UUID, id_status_aluno UUID, ra VARCHAR(80), rm VARCHAR(80),
                    emancipado BOOLEAN, data_ingresso DATE, data_saida DATE, motivo_saida VARCHAR(500),
                    ativo BOOLEAN, created_at TIMESTAMP
                )
                """);
        return jdbc;
    }

    private JdbcTemplate targetDatabase() {
        String url = "jdbc:h2:mem:people-backfill-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/people-readmodel/migration")
                .load()
                .migrate();
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }
}
