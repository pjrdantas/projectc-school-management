package br.com.escola.dashboard.adapter.in.web;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM dashboard_usuario_configuracao WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'dash-user-%')",
                "DELETE FROM dashboard_widget WHERE codigo LIKE 'DASH-USER-%'",
                "DELETE FROM dashboard WHERE codigo LIKE 'DASH-USER-%'",
                "DELETE FROM publico_dashboard WHERE codigo LIKE 'DASH-USER-%'",
                "DELETE FROM usuario WHERE username LIKE 'dash-user-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM dashboard_usuario_configuracao WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'dash-user-%')",
                "DELETE FROM dashboard_widget WHERE codigo LIKE 'DASH-USER-%'",
                "DELETE FROM dashboard WHERE codigo LIKE 'DASH-USER-%'",
                "DELETE FROM publico_dashboard WHERE codigo LIKE 'DASH-USER-%'",
                "DELETE FROM usuario WHERE username LIKE 'dash-user-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DashboardUsuarioConfiguracaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveSalvarAtualizarListarEExcluirConfiguracaoPorUsuario() throws Exception {
        UUID usuarioId = criarUsuario();
        UUID publicoId = criarPublico("dash-user-professor", "Professor usuario");
        UUID dashboardId = criarDashboard(publicoId, "dash-user-professor-geral", "Dashboard Professor Usuario");
        UUID widgetId = criarWidget(dashboardId, "dash-user-aulas", "Aulas");

        salvarConfiguracao(usuarioId, widgetId, false, 3, """
                {"periodo":"ULTIMOS_30_DIAS","tamanho":"LARGO"}
                """)
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.dashboardWidgetId").value(widgetId.toString()))
                .andExpect(jsonPath("$.visivel").value(false))
                .andExpect(jsonPath("$.ordem").value(3))
                .andExpect(jsonPath("$.configuracaoJson").value("{\"periodo\":\"ULTIMOS_30_DIAS\",\"tamanho\":\"LARGO\"}"));

        salvarConfiguracao(usuarioId, widgetId, true, 1, """
                {"periodo":"ANO_LETIVO","colunas":["turma","status"]}
                """)
                .andExpect(jsonPath("$.visivel").value(true))
                .andExpect(jsonPath("$.ordem").value(1))
                .andExpect(jsonPath("$.configuracaoJson").value("{\"periodo\":\"ANO_LETIVO\",\"colunas\":[\"turma\",\"status\"]}"));

        mockMvc.perform(get("/api/dashboard/usuarios/{usuarioId}/configuracoes", usuarioId)
                        .param("dashboardId", dashboardId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.widgetCodigo == 'DASH-USER-AULAS')].ordem")
                        .value(hasItem(1)));

        mockMvc.perform(delete("/api/dashboard/usuarios/{usuarioId}/widgets/{dashboardWidgetId}/configuracao",
                        usuarioId,
                        widgetId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/dashboard/usuarios/{usuarioId}/configuracoes", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoParaUsuarioInexistente() throws Exception {
        UUID publicoId = criarPublico("dash-user-secretaria", "Secretaria usuario");
        UUID dashboardId = criarDashboard(publicoId, "dash-user-secretaria-geral", "Dashboard Secretaria Usuario");
        UUID widgetId = criarWidget(dashboardId, "dash-user-documentos", "Documentos");

        String requestBody = """
                {
                  "visivel": true,
                  "ordem": 1
                }
                """;

        mockMvc.perform(put("/api/dashboard/usuarios/{usuarioId}/widgets/{dashboardWidgetId}/configuracao",
                        UUID.randomUUID(),
                        widgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveRejeitarConfiguracaoJsonInvalida() throws Exception {
        UUID usuarioId = criarUsuario();
        UUID publicoId = criarPublico("dash-user-diretor", "Diretor usuario");
        UUID dashboardId = criarDashboard(publicoId, "dash-user-diretor-geral", "Dashboard Diretor Usuario");
        UUID widgetId = criarWidget(dashboardId, "dash-user-indicadores", "Indicadores");

        String requestBody = """
                {
                  "visivel": true,
                  "ordem": 1,
                  "configuracaoJson": "{json-invalido"
                }
                """;

        mockMvc.perform(put("/api/dashboard/usuarios/{usuarioId}/widgets/{dashboardWidgetId}/configuracao",
                        usuarioId,
                        widgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions salvarConfiguracao(
            UUID usuarioId,
            UUID widgetId,
            boolean visivel,
            int ordem,
            String configuracaoJson) throws Exception {
        String requestBody = """
                {
                  "visivel": %s,
                  "ordem": %d,
                  "configuracaoJson": %s
                }
                """.formatted(visivel, ordem, objectMapper.writeValueAsString(configuracaoJson));

        return mockMvc.perform(put("/api/dashboard/usuarios/{usuarioId}/widgets/{dashboardWidgetId}/configuracao",
                        usuarioId,
                        widgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    private UUID criarUsuario() {
        UUID usuarioId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at)
                VALUES (?, ?, 'Usuário Dashboard', ?, 'hash-dashboard-usuario', true, CURRENT_TIMESTAMP)
                """, usuarioId, "dash-user-" + System.nanoTime(), "dash-user-%s@example.com".formatted(usuarioId));
        return usuarioId;
    }

    private UUID criarPublico(String codigo, String descricao) throws Exception {
        String requestBody = """
                {
                  "codigo": "%s",
                  "descricao": "%s"
                }
                """.formatted(codigo, descricao);

        String responseBody = mockMvc.perform(post("/api/dashboard/configuracoes/publicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo.toUpperCase()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarDashboard(UUID publicoId, String codigo, String nome) throws Exception {
        String requestBody = """
                {
                  "publicoDashboardId": "%s",
                  "codigo": "%s",
                  "nome": "%s",
                  "descricao": "Dashboard criado para configuração por usuário",
                  "ativo": true
                }
                """.formatted(publicoId, codigo, nome);

        String responseBody = mockMvc.perform(post("/api/dashboard/configuracoes/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo.toUpperCase()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarWidget(UUID dashboardId, String codigo, String titulo) throws Exception {
        String requestBody = """
                {
                  "dashboardId": "%s",
                  "codigo": "%s",
                  "titulo": "%s",
                  "descricao": "Widget criado para configuração por usuário",
                  "tipoWidget": "card",
                  "ordem": 1,
                  "queryReferencia": "dashboard_usuario",
                  "ativo": true
                }
                """.formatted(dashboardId, codigo, titulo);

        String responseBody = mockMvc.perform(post("/api/dashboard/configuracoes/widgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo.toUpperCase()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }
}
