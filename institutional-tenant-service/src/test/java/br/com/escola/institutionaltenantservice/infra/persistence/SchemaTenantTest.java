package br.com.escola.institutionaltenantservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SchemaTenantTest {

    @Test
    void deveCriarSomenteEscolasEVinculosDoDominioInstitucional() {
        String url = "jdbc:h2:mem:institutional-tenant-schema;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/institutional-tenant/migration")
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

        assertThat(tables).containsExactly("escola", "usuario_escola");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM information_schema.table_constraints
                WHERE table_schema = 'PUBLIC'
                  AND table_name = 'USUARIO_ESCOLA'
                  AND constraint_type = 'FOREIGN KEY'
                """, Integer.class)).isOne();
    }
}
