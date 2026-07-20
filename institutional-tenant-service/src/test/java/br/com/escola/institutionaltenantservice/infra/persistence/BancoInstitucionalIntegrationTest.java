package br.com.escola.institutionaltenantservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BancoInstitucionalIntegrationTest {

    private static final String SOURCE_URL =
            "jdbc:h2:mem:institutional-cutover-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String TARGET_URL =
            "jdbc:h2:mem:institutional-cutover-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final UUID ESCOLA_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final UUID USUARIO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000402");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate target;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        prepararBancos();
        registry.add("spring.datasource.url", () -> TARGET_URL);
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("institutional-tenant.internal-api.token", () -> "institutional-token");
        registry.add("institutional-tenant.persistence.school-load.enabled", () -> true);
        registry.add("institutional-tenant.persistence.school-load.source-url", () -> SOURCE_URL);
        registry.add("institutional-tenant.persistence.school-load.source-username", () -> "sa");
        registry.add("institutional-tenant.persistence.school-load.source-password", () -> "");
        registry.add("institutional-tenant.persistence.user-school-load.enabled", () -> true);
        registry.add("institutional-tenant.persistence.user-school-load.source-url", () -> SOURCE_URL);
        registry.add("institutional-tenant.persistence.user-school-load.source-username", () -> "sa");
        registry.add("institutional-tenant.persistence.user-school-load.source-password", () -> "");
    }

    @Test
    void deveMigrarCarregarEOperarSomenteNoBancoInstitucional() throws Exception {
        mockMvc.perform(get("/internal/v1/tenant/escolas")
                        .header("X-Internal-Token", "institutional-token")
                        .header("X-Correlation-Id", "corr-banco-institucional")
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer institutional-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").value(ESCOLA_ID.toString()))
                .andExpect(jsonPath("$[0].escolaNome").value("Escola do corte"));

        mockMvc.perform(post("/internal/v1/escolas")
                        .header("X-Internal-Token", "institutional-token")
                        .header("X-Correlation-Id", "corr-banco-institucional-write")
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Escola-Id", ESCOLA_ID)
                        .contentType("application/json")
                        .content("""
                                {"nome":"Escola criada no banco proprio"}
                                """))
                .andExpect(status().isCreated());

        assertThat(target.queryForObject("SELECT COUNT(1) FROM escola", Integer.class)).isEqualTo(2);
        assertThat(target.queryForObject("SELECT COUNT(1) FROM usuario_escola", Integer.class)).isOne();
        assertThat(tabelasDeNegocio()).containsExactly("escola", "usuario_escola");

        JdbcTemplate source = jdbc(SOURCE_URL);
        assertThat(source.queryForObject("SELECT COUNT(1) FROM escola", Integer.class)).isOne();
    }

    private List<String> tabelasDeNegocio() {
        return target.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'PUBLIC'
                  AND table_name <> 'flyway_schema_history'
                ORDER BY table_name
                """, String.class).stream().map(String::toLowerCase).toList();
    }

    private static void prepararBancos() {
        limpar(TARGET_URL);
        limpar(SOURCE_URL);
        migrar(SOURCE_URL);

        JdbcTemplate source = jdbc(SOURCE_URL);
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 17, 40);
        source.update("""
                INSERT INTO escola (id_escola, nome, ativo, created_at)
                VALUES (?, ?, ?, ?)
                """, ESCOLA_ID, "Escola do corte", true, now);
        source.update("""
                INSERT INTO usuario_escola (id_usuario_escola, id_usuario, id_escola, created_at)
                VALUES (?, ?, ?, ?)
                """, UUID.randomUUID(), USUARIO_ID, ESCOLA_ID, now.plusMinutes(1));
    }

    private static void limpar(String url) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/institutional-tenant/migration")
                .cleanDisabled(false)
                .load()
                .clean();
    }

    private static void migrar(String url) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/institutional-tenant/migration")
                .load()
                .migrate();
    }

    private static JdbcTemplate jdbc(String url) {
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }
}
