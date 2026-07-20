package br.com.escola.institutionaltenantservice.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

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
                    codigo_inep VARCHAR(30),
                    cnpj VARCHAR(18),
                    telefone VARCHAR(30),
                    email VARCHAR(150),
                    id_endereco UUID,
                    ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE usuario_escola (
                    id_usuario_escola UUID PRIMARY KEY,
                    id_usuario UUID NOT NULL,
                    id_escola UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    CONSTRAINT fk_usuario_escola_escola FOREIGN KEY (id_escola)
                        REFERENCES escola (id_escola) ON DELETE CASCADE,
                    CONSTRAINT uk_usuario_escola UNIQUE (id_usuario, id_escola)
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
    void deveAdministrarCicloCompletoDeEscola() throws Exception {
        UUID enderecoId = UUID.randomUUID();
        String response = mockMvc.perform(post("/internal/v1/escolas")
                        .headers(internalHeaders("corr-escola-create"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome":" Escola Administrada ",
                                  "codigoInep":" INEP-100 ",
                                  "cnpj":"12.345.678/0001-90",
                                  "telefone":" 11999999999 ",
                                  "email":"CONTATO@ESCOLA.COM",
                                  "enderecoId":"%s"
                                }
                                """.formatted(enderecoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Escola Administrada"))
                .andExpect(jsonPath("$.email").value("contato@escola.com"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn().getResponse().getContentAsString();
        UUID escolaId = UUID.fromString(JsonPath.read(response, "$.id"));

        mockMvc.perform(get("/internal/v1/escolas/{id}", escolaId)
                        .headers(internalHeaders("corr-escola-get")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoInep").value("INEP-100"));

        mockMvc.perform(put("/internal/v1/escolas/{id}", escolaId)
                        .headers(internalHeaders("corr-escola-update"))
                        .contentType("application/json")
                        .content("""
                                {"nome":"Escola Atualizada","ativo":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Escola Atualizada"))
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        inserirVinculo(UUID.randomUUID(), escolaId);
        mockMvc.perform(delete("/internal/v1/escolas/{id}", escolaId)
                        .headers(internalHeaders("corr-escola-conflict")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));

        jdbcTemplate.update("DELETE FROM usuario_escola WHERE id_escola = ?", escolaId);
        mockMvc.perform(delete("/internal/v1/escolas/{id}", escolaId)
                        .headers(internalHeaders("corr-escola-delete")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/internal/v1/escolas/{id}", escolaId)
                        .headers(internalHeaders("corr-escola-missing")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAdministrarVinculoUsuarioEscolaDeFormaIdempotente() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        inserirEscola(escolaId, "Escola Vinculada");

        String request = """
                {"usuarioId":"%s","escolaId":"%s"}
                """.formatted(usuarioId, escolaId);
        String response = mockMvc.perform(post("/internal/v1/vinculos-usuario-escola")
                        .headers(internalHeaders("corr-vinculo-create"))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andReturn().getResponse().getContentAsString();
        UUID vinculoId = UUID.fromString(JsonPath.read(response, "$.id"));

        mockMvc.perform(post("/internal/v1/vinculos-usuario-escola")
                        .headers(internalHeaders("corr-vinculo-repeat"))
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vinculoId.toString()));

        mockMvc.perform(get("/internal/v1/vinculos-usuario-escola")
                        .param("usuarioId", usuarioId.toString())
                        .param("escolaId", escolaId.toString())
                        .headers(internalHeaders("corr-vinculo-list")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(vinculoId.toString()));

        mockMvc.perform(get("/internal/v1/vinculos-usuario-escola/{id}", vinculoId)
                        .headers(internalHeaders("corr-vinculo-get")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        mockMvc.perform(delete("/internal/v1/vinculos-usuario-escola/{id}", vinculoId)
                        .headers(internalHeaders("corr-vinculo-delete")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/internal/v1/vinculos-usuario-escola/{id}", vinculoId)
                        .headers(internalHeaders("corr-vinculo-missing")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRecusarVinculoComEscolaInexistente() throws Exception {
        mockMvc.perform(post("/internal/v1/vinculos-usuario-escola")
                        .headers(internalHeaders("corr-vinculo-escola-ausente"))
                        .contentType("application/json")
                        .content("""
                                {"usuarioId":"%s","escolaId":"%s"}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
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

    private HttpHeaders internalHeaders(String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Internal-Token", "institutional-token");
        headers.add("X-Correlation-Id", correlationId);
        headers.add("X-Usuario-Id", UUID.randomUUID().toString());
        headers.add("X-Escola-Id", UUID.randomUUID().toString());
        return headers;
    }
}

