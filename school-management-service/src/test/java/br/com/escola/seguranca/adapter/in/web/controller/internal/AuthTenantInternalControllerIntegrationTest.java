package br.com.escola.seguranca.adapter.in.web.controller.internal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM sessao_autenticacao WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor52tenant%')",
                "DELETE FROM usuario_escola WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor52tenant%')",
                "DELETE FROM usuario_perfil WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor52tenant%')",
                "DELETE FROM usuario WHERE username LIKE 'professor52tenant%'",
                "DELETE FROM escola WHERE nome LIKE 'Escola Tenant 52%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM sessao_autenticacao WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor52tenant%')",
                "DELETE FROM usuario_escola WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor52tenant%')",
                "DELETE FROM usuario_perfil WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor52tenant%')",
                "DELETE FROM usuario WHERE username LIKE 'professor52tenant%'",
                "DELETE FROM escola WHERE nome LIKE 'Escola Tenant 52%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthTenantInternalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveListarEscolasDaSessaoESelecionarNovaEscolaAtiva() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaPadraoId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID outraEscolaId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO escola (id_escola, nome, ativo, created_at)
                VALUES (?, 'Escola Tenant 52 B', true, CURRENT_TIMESTAMP)
                """, outraEscolaId);
        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, id_escola, created_at)
                VALUES (?, 'professor52tenant', 'Professor Tenant 52', 'professor52tenant@example.com', ?, true, ?, CURRENT_TIMESTAMP)
                """, usuarioId, passwordEncoder.encode("senha123"), escolaPadraoId);
        jdbcTemplate.update("""
                INSERT INTO usuario_escola (id_usuario_escola, id_usuario, id_escola, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), usuarioId, escolaPadraoId);
        jdbcTemplate.update("""
                INSERT INTO usuario_escola (id_usuario_escola, id_usuario, id_escola, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), usuarioId, outraEscolaId);

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "professor52tenant",
                                  "senha": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value(escolaPadraoId.toString()))
                .andReturn();

        JsonNode payload = objectMapper.readTree(login.getResponse().getContentAsString());
        String accessToken = payload.get("accessToken").asText();

        mockMvc.perform(get("/internal/auth/escolas")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").exists())
                .andExpect(jsonPath("$[1].escolaId").exists());

        mockMvc.perform(post("/internal/auth/escola-ativa")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "escolaId": "%s"
                                }
                                """.formatted(outraEscolaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(outraEscolaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola Tenant 52 B"))
                .andExpect(jsonPath("$.username").value("professor52tenant"));

        mockMvc.perform(get("/api/auth/contexto-atual")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value(outraEscolaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola Tenant 52 B"));
    }
}
