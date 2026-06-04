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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM dashboard_usuario_configuracao WHERE id_dashboard_widget IN (SELECT id_dashboard_widget FROM dashboard_widget WHERE codigo LIKE 'DASH-CONF-%')",
                "DELETE FROM dashboard_widget WHERE codigo LIKE 'DASH-CONF-%'",
                "DELETE FROM dashboard WHERE codigo LIKE 'DASH-CONF-%'",
                "DELETE FROM publico_dashboard WHERE codigo LIKE 'DASH-CONF-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM dashboard_usuario_configuracao WHERE id_dashboard_widget IN (SELECT id_dashboard_widget FROM dashboard_widget WHERE codigo LIKE 'DASH-CONF-%')",
                "DELETE FROM dashboard_widget WHERE codigo LIKE 'DASH-CONF-%'",
                "DELETE FROM dashboard WHERE codigo LIKE 'DASH-CONF-%'",
                "DELETE FROM publico_dashboard WHERE codigo LIKE 'DASH-CONF-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DashboardConfiguracaoAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void deveConfigurarDashboardComPublicoEWidgets() throws Exception {
        UUID publicoId = criarPublico("dash-conf-diretor", "Diretoria teste");
        UUID dashboardId = criarDashboard(publicoId, "dash-conf-diretor-geral", "Dashboard Diretor Teste");
        UUID widgetId = criarWidget(dashboardId, "dash-conf-matriculas", "Matrículas");

        mockMvc.perform(get("/api/dashboard/configuracoes/publicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'DASH-CONF-DIRETOR')].descricao")
                        .value(hasItem("Diretoria teste")));

        mockMvc.perform(get("/api/dashboard/configuracoes/dashboards")
                        .param("publicoCodigo", "dash-conf-diretor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'DASH-CONF-DIRETOR-GERAL')].nome")
                        .value(hasItem("Dashboard Diretor Teste")));

        mockMvc.perform(get("/api/dashboard/configuracoes/dashboards/{dashboardId}/widgets", dashboardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'DASH-CONF-MATRICULAS')].titulo")
                        .value(hasItem("Matrículas")));

        atualizarWidget(widgetId, dashboardId);
        atualizarDashboard(dashboardId, publicoId);
        atualizarPublico(publicoId);

        mockMvc.perform(delete("/api/dashboard/configuracoes/widgets/{id}", widgetId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/dashboard/configuracoes/dashboards/{id}", dashboardId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/dashboard/configuracoes/publicos/{id}", publicoId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deveBloquearDashboardDuplicado() throws Exception {
        UUID publicoId = criarPublico("dash-conf-secretaria", "Secretaria teste");
        criarDashboard(publicoId, "dash-conf-secretaria-operacional", "Dashboard Secretaria Teste");

        String requestBody = """
                {
                  "publicoDashboardId": "%s",
                  "codigo": "dash-conf-secretaria-operacional",
                  "nome": "Dashboard duplicado",
                  "ativo": true
                }
                """.formatted(publicoId);

        mockMvc.perform(post("/api/dashboard/configuracoes/dashboards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
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
                  "descricao": "Dashboard criado pelo teste",
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
                  "descricao": "Widget criado pelo teste",
                  "tipoWidget": "card",
                  "ordem": 1,
                  "queryReferencia": "matriculas_pendentes",
                  "ativo": true
                }
                """.formatted(dashboardId, codigo, titulo);

        String responseBody = mockMvc.perform(post("/api/dashboard/configuracoes/widgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo.toUpperCase()))
                .andExpect(jsonPath("$.tipoWidget").value("CARD"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private void atualizarPublico(UUID publicoId) throws Exception {
        String requestBody = """
                {
                  "codigo": "dash-conf-diretor",
                  "descricao": "Diretoria teste atualizada"
                }
                """;

        mockMvc.perform(put("/api/dashboard/configuracoes/publicos/{id}", publicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Diretoria teste atualizada"));
    }

    private void atualizarDashboard(UUID dashboardId, UUID publicoId) throws Exception {
        String requestBody = """
                {
                  "publicoDashboardId": "%s",
                  "codigo": "dash-conf-diretor-geral",
                  "nome": "Dashboard Diretor Atualizado",
                  "descricao": "Dashboard atualizado pelo teste",
                  "ativo": true
                }
                """.formatted(publicoId);

        mockMvc.perform(put("/api/dashboard/configuracoes/dashboards/{id}", dashboardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Dashboard Diretor Atualizado"));
    }

    private void atualizarWidget(UUID widgetId, UUID dashboardId) throws Exception {
        String requestBody = """
                {
                  "dashboardId": "%s",
                  "codigo": "dash-conf-matriculas",
                  "titulo": "Matrículas atualizadas",
                  "descricao": "Widget atualizado pelo teste",
                  "tipoWidget": "lista",
                  "ordem": 2,
                  "queryReferencia": "matriculas_por_status",
                  "ativo": true
                }
                """.formatted(dashboardId);

        mockMvc.perform(put("/api/dashboard/configuracoes/widgets/{id}", widgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Matrículas atualizadas"))
                .andExpect(jsonPath("$.tipoWidget").value("LISTA"));
    }
}
