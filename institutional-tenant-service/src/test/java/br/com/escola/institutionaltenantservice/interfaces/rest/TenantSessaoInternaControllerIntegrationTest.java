package br.com.escola.institutionaltenantservice.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TenantSessaoInternaControllerIntegrationTest {

    private static final String SHARED_URL =
            "jdbc:h2:mem:institutional-shared-without-tables;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("tenantReadJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("institutional-tenant.internal-api.token", () -> "institutional-token");
        registry.add("spring.datasource.url", () -> SHARED_URL);
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("institutional-tenant.persistence.read.url", () ->
                "jdbc:h2:mem:institutional-local-read;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        registry.add("institutional-tenant.persistence.read.username", () -> "sa");
        registry.add("institutional-tenant.persistence.read.password", () -> "");
    }

    @BeforeEach
    void prepararBanco() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS usuario_escola");
        jdbcTemplate.execute("DROP TABLE IF EXISTS escola");

        jdbcTemplate.execute("""
                CREATE TABLE escola (
                    id_escola UUID PRIMARY KEY,
                    nome VARCHAR(150) NOT NULL,
                    ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE usuario_escola (
                    id_usuario_escola UUID PRIMARY KEY,
                    id_usuario UUID NOT NULL,
                    id_escola UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);
    }

    @Test
    void deveListarEscolasDisponiveisNoContratoInterno() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaAtivaId = UUID.randomUUID();
        UUID outraEscolaId = UUID.randomUUID();

        inserirEscola(escolaAtivaId, "Escola Ativa");
        inserirEscola(outraEscolaId, "Escola Reserva");
        inserirVinculo(usuarioId, escolaAtivaId);
        inserirVinculo(usuarioId, outraEscolaId);

        mockMvc.perform(get("/internal/v1/tenant/escolas")
                        .header("X-Internal-Token", "institutional-token")
                        .header("X-Correlation-Id", "corr-tenant-1")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaAtivaId)
                        .header("Authorization", "Bearer tenant-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").value(escolaAtivaId.toString()))
                .andExpect(jsonPath("$[0].ativa").value(true))
                .andExpect(jsonPath("$[1].escolaNome").value("Escola Reserva"));

        JdbcTemplate sharedJdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(SHARED_URL, "sa", ""));
        Integer sharedTables = sharedJdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM information_schema.tables
                WHERE table_schema = 'PUBLIC'
                  AND table_name IN ('ESCOLA', 'USUARIO_ESCOLA')
                """, Integer.class);
        org.assertj.core.api.Assertions.assertThat(sharedTables).isZero();
    }

    @Test
    void deveRetornarTenantAtivoDerivadoDaListaDeEscolasDaSessao() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaAtivaId = UUID.randomUUID();

        inserirEscola(escolaAtivaId, "Escola Principal");
        inserirVinculo(usuarioId, escolaAtivaId);

        mockMvc.perform(get("/internal/v1/tenant/ativa")
                        .header("X-Internal-Token", "institutional-token")
                        .header("X-Correlation-Id", "corr-tenant-2")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaAtivaId)
                        .header("Authorization", "Bearer tenant-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value(escolaAtivaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola Principal"));
    }

    @Test
    void deveRetornarTenantAtivoPelaEscolaDoContextoQuandoNaoHouverVinculo() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaAtivaId = UUID.randomUUID();

        inserirEscola(escolaAtivaId, "Escola Contexto");

        mockMvc.perform(get("/internal/v1/tenant/ativa")
                        .header("X-Internal-Token", "institutional-token")
                        .header("X-Correlation-Id", "corr-tenant-2b")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaAtivaId)
                        .header("Authorization", "Bearer tenant-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value(escolaAtivaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola Contexto"));
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/tenant/escolas")
                        .header("X-Correlation-Id", "corr-tenant-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer tenant-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    private void inserirEscola(UUID escolaId, String nome) {
        jdbcTemplate.update("""
                INSERT INTO escola (id_escola, nome, ativo, created_at)
                VALUES (?, ?, ?, ?)
                """, escolaId, nome, true, Timestamp.valueOf(LocalDateTime.now()));
    }

    private void inserirVinculo(UUID usuarioId, UUID escolaId) {
        jdbcTemplate.update("""
                INSERT INTO usuario_escola (id_usuario_escola, id_usuario, id_escola, created_at)
                VALUES (?, ?, ?, ?)
                """, UUID.randomUUID(), usuarioId, escolaId, Timestamp.valueOf(LocalDateTime.now()));
    }
}

