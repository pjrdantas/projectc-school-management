package br.com.escola.peopleservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class PeopleDatasourceIntegrationTest {

    private static final String SOURCE_URL =
            "jdbc:h2:mem:people-cutover-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String TARGET_URL =
            "jdbc:h2:mem:people-cutover-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final UUID ALUNO_ID = UUID.fromString("00000000-0000-0000-0000-000000000801");

    @Autowired
    private JdbcTemplate target;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        prepararOrigem();
        registry.add("spring.datasource.url", () -> TARGET_URL);
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("people.persistence.backfill.enabled", () -> true);
        registry.add("people.persistence.backfill.source-url", () -> SOURCE_URL);
        registry.add("people.persistence.backfill.source-username", () -> "sa");
        registry.add("people.persistence.backfill.source-password", () -> "");
        registry.add("people.internal-api.token", () -> "people-token");
    }

    @Test
    void deveAplicarFlywayECarregarAlunoNoDatasourceProprio() {
        assertThat(target.queryForObject("SELECT COUNT(1) FROM status_aluno", Integer.class)).isOne();
        assertThat(target.queryForObject("SELECT COUNT(1) FROM aluno WHERE id_aluno = ?", Integer.class, ALUNO_ID))
                .isOne();
        assertThat(target.queryForObject("SELECT escola_nome FROM pessoa", String.class)).isEqualTo("Escola corte");
        assertThat(target.queryForObject("SELECT id_escola FROM aluno WHERE id_aluno = ?", UUID.class, ALUNO_ID))
                .isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000047"));
    }

    private static void prepararOrigem() {
        Flyway.configure()
                .dataSource(SOURCE_URL, "sa", "")
                .locations("classpath:db/people/migration")
                .cleanDisabled(false)
                .load()
                .clean();
        Flyway.configure()
                .dataSource(SOURCE_URL, "sa", "")
                .locations("classpath:db/people/migration")
                .load()
                .migrate();

        JdbcTemplate source = new JdbcTemplate(new DriverManagerDataSource(SOURCE_URL, "sa", ""));
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID pessoaId = UUID.fromString("00000000-0000-0000-0000-000000000802");
        UUID tipoPessoaId = UUID.fromString("00000000-0000-0000-0000-000000000803");
        UUID statusAlunoId = UUID.fromString("00000000-0000-0000-0000-000000000804");
        LocalDateTime now = LocalDateTime.of(2026, 7, 21, 13, 0);

        source.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150) NOT NULL)");
        source.update("INSERT INTO escola (id_escola, nome) VALUES (?, ?)", escolaId, "Escola corte");
        source.update("""
                INSERT INTO tipo_pessoa (id_tipo_pessoa, codigo, descricao, created_at)
                VALUES (?, ?, ?, ?)
                """, tipoPessoaId, "ALUNO", "Aluno", now);
        source.update("INSERT INTO status_aluno (id_status_aluno, codigo, descricao) VALUES (?, ?, ?)",
                statusAlunoId, "ATIVO", "Ativo");
        source.update("""
                INSERT INTO pessoa (
                    id_pessoa, id_escola, escola_nome, nome_completo, cpf, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, pessoaId, escolaId, "Escola corte", "Aluno de Corte", "33333333333", true, now);
        source.update("""
                INSERT INTO pessoa_tipo_pessoa (id_pessoa_tipo_pessoa, id_pessoa, id_tipo_pessoa, created_at)
                VALUES (?, ?, ?, ?)
                """, UUID.randomUUID(), pessoaId, tipoPessoaId, now);
        source.update("""
                INSERT INTO aluno (
                    id_aluno, id_pessoa, id_escola, id_status_aluno, nome_completo, cpf,
                    emancipado, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, ALUNO_ID, pessoaId, escolaId, statusAlunoId, "Aluno de Corte", "33333333333",
                false, true, now);
    }
}
