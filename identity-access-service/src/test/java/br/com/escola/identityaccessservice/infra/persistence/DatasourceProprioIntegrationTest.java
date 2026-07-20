package br.com.escola.identityaccessservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DatasourceProprioIntegrationTest {

    private static final String SOURCE_URL =
            "jdbc:h2:mem:identity-cutover-source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String TARGET_URL =
            "jdbc:h2:mem:identity-cutover-target;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate target;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        prepararOrigem();
        registry.add("spring.datasource.url", () -> TARGET_URL);
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("identity-access.internal-api.token", () -> "identity-token");
        registry.add("identity-access.session-cleanup.enabled", () -> false);
        registry.add("identity-access.persistence.backfill.enabled", () -> true);
        registry.add("identity-access.persistence.backfill.source-url", () -> SOURCE_URL);
        registry.add("identity-access.persistence.backfill.source-username", () -> "sa");
        registry.add("identity-access.persistence.backfill.source-password", () -> "");
    }

    @Test
    void deveMigrarDadosEAutenticarUsandoSomenteODatasourceProprio() throws Exception {
        mockMvc.perform(post("/internal/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"login":"admin.corte","senha":"senha-segura"}
                                """)
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-cutover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(USUARIO_ID.toString()))
                .andExpect(jsonPath("$.perfis[0]").value("ADMIN"));

        assertThat(target.queryForObject("SELECT COUNT(1) FROM usuario", Integer.class)).isOne();
        assertThat(tabelasDeNegocio()).containsExactly(
                "perfil",
                "perfil_permissao",
                "permissao",
                "sessao_autenticacao",
                "usuario",
                "usuario_perfil");
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

    private static void prepararOrigem() {
        Flyway.configure()
                .dataSource(SOURCE_URL, "sa", "")
                .locations("classpath:db/identity-access/migration")
                .cleanDisabled(false)
                .load()
                .clean();
        Flyway.configure()
                .dataSource(SOURCE_URL, "sa", "")
                .locations("classpath:db/identity-access/migration")
                .load()
                .migrate();

        JdbcTemplate source = new JdbcTemplate(new DriverManagerDataSource(SOURCE_URL, "sa", ""));
        UUID perfilId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 17, 0);
        source.update("""
                INSERT INTO perfil (id_perfil, codigo, nome, created_at)
                VALUES (?, ?, ?, ?)
                """, perfilId, "ADMIN", "Administrador", now);
        source.update("""
                INSERT INTO usuario (
                    id_usuario, username, nome, email, senha_hash, ativo, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, USUARIO_ID, "admin.corte", "Administrador do corte",
                "admin.corte@escola.com", new BCryptPasswordEncoder().encode("senha-segura"), true, now);
        source.update("""
                INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
                VALUES (?, ?, ?)
                """, UUID.randomUUID(), USUARIO_ID, perfilId);
    }
}
