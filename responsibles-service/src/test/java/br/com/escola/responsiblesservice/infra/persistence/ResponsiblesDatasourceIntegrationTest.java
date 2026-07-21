package br.com.escola.responsiblesservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class ResponsiblesDatasourceIntegrationTest {

    private static final String SOURCE_URL =
            "jdbc:h2:mem:responsibles-cutover-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String TARGET_URL =
            "jdbc:h2:mem:responsibles-cutover-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final UUID RESPONSAVEL_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");

    @Autowired
    private JdbcTemplate target;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        prepararOrigem();
        registry.add("spring.datasource.url", () -> TARGET_URL);
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("responsibles.persistence.backfill.enabled", () -> true);
        registry.add("responsibles.persistence.backfill.source-url", () -> SOURCE_URL);
        registry.add("responsibles.persistence.backfill.source-username", () -> "sa");
        registry.add("responsibles.persistence.backfill.source-password", () -> "");
    }

    @Test
    void deveAplicarFlywayECarregarResponsavelNoDatasourceProprio() {
        assertThat(target.queryForObject("SELECT COUNT(1) FROM parentesco", Integer.class)).isEqualTo(2);
        assertThat(target.queryForObject("SELECT COUNT(1) FROM responsavel WHERE id_responsavel = ?", Integer.class,
                RESPONSAVEL_ID)).isOne();
        assertThat(target.queryForObject("SELECT escola_nome FROM responsavel WHERE id_responsavel = ?", String.class,
                RESPONSAVEL_ID)).isEqualTo("Escola corte");
        assertThat(target.queryForObject("SELECT COUNT(1) FROM aluno_responsavel", Integer.class)).isOne();
    }

    private static void prepararOrigem() {
        JdbcTemplate source = new JdbcTemplate(new DriverManagerDataSource(SOURCE_URL, "sa", ""));
        source.execute("DROP ALL OBJECTS");
        source.execute("CREATE TABLE escola (id_escola UUID PRIMARY KEY, nome VARCHAR(150) NOT NULL)");
        source.execute("""
                CREATE TABLE pessoa (
                    id_pessoa UUID PRIMARY KEY, id_escola UUID NOT NULL, nome_completo VARCHAR(150) NOT NULL,
                    cpf VARCHAR(14), email VARCHAR(150), telefone VARCHAR(20), rg VARCHAR(20), ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP
                )
                """);
        source.execute("CREATE TABLE responsavel (id_responsavel UUID PRIMARY KEY, id_pessoa UUID NOT NULL, created_at TIMESTAMP NOT NULL)");
        source.execute("""
                CREATE TABLE endereco (
                    id_endereco UUID PRIMARY KEY, cep VARCHAR(14), logradouro VARCHAR(200), numero VARCHAR(20),
                    complemento VARCHAR(120), bairro VARCHAR(120), cidade VARCHAR(120), uf VARCHAR(2)
                )
                """);
        source.execute("""
                CREATE TABLE pessoa_endereco (
                    id_pessoa_endereco UUID PRIMARY KEY, id_pessoa UUID NOT NULL, id_endereco UUID NOT NULL, principal BOOLEAN NOT NULL
                )
                """);
        source.execute("CREATE TABLE parentesco (id_parentesco UUID PRIMARY KEY, codigo VARCHAR(40), descricao VARCHAR(120))");
        source.execute("""
                CREATE TABLE aluno_responsavel (
                    id_aluno_responsavel UUID PRIMARY KEY, id_aluno UUID NOT NULL, id_responsavel UUID NOT NULL,
                    id_parentesco UUID, responsavel_financeiro BOOLEAN, responsavel_pedagogico BOOLEAN,
                    autorizado_retirar BOOLEAN, created_at TIMESTAMP NOT NULL
                )
                """);

        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID pessoaId = UUID.fromString("00000000-0000-0000-0000-000000000902");
        UUID parentescoId = UUID.fromString("00000000-0000-0000-0000-000000000903");
        LocalDateTime now = LocalDateTime.of(2026, 7, 21, 14, 30);
        source.update("INSERT INTO escola (id_escola, nome) VALUES (?, ?)", escolaId, "Escola corte");
        source.update("""
                INSERT INTO pessoa (id_pessoa, id_escola, nome_completo, cpf, ativo, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, pessoaId, escolaId, "Responsavel de Corte", "12345678901", true, now, now);
        source.update("INSERT INTO responsavel (id_responsavel, id_pessoa, created_at) VALUES (?, ?, ?)",
                RESPONSAVEL_ID, pessoaId, now);
        source.update("INSERT INTO parentesco (id_parentesco, codigo, descricao) VALUES (?, ?, ?)",
                parentescoId, "MAE", "Mae");
        source.update("""
                INSERT INTO aluno_responsavel (
                    id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco,
                    responsavel_financeiro, responsavel_pedagogico, autorizado_retirar, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000904"), RESPONSAVEL_ID,
                parentescoId, true, false, true, now);
    }
}
