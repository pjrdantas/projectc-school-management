package br.com.escola.identityaccessservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SchemaMigrationTest {

    @Test
    void deveCriarSomenteAsSeisTabelasDoDominioDeAcesso() {
        String url = "jdbc:h2:mem:identity-schema;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/identity-access/migration")
                .load()
                .migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'PUBLIC'
                  AND table_name <> 'flyway_schema_history'
                ORDER BY table_name
                """, String.class).stream().map(String::toLowerCase).toList();

        assertThat(tables).containsExactly(
                "perfil",
                "perfil_permissao",
                "permissao",
                "sessao_autenticacao",
                "usuario",
                "usuario_perfil");
    }
}
