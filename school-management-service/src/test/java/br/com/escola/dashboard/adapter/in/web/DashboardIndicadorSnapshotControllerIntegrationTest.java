package br.com.escola.dashboard.adapter.in.web;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                "DELETE FROM dashboard_indicador_snapshot WHERE codigo_indicador LIKE 'DASH-SNAP-%'",
                "DELETE FROM publico_dashboard WHERE codigo LIKE 'DASH-SNAP-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM dashboard_indicador_snapshot WHERE codigo_indicador LIKE 'DASH-SNAP-%'",
                "DELETE FROM publico_dashboard WHERE codigo LIKE 'DASH-SNAP-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DashboardIndicadorSnapshotControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveSalvarAtualizarListarEExcluirSnapshot() throws Exception {
        UUID publicoId = criarPublico("DASH-SNAP-DIRETOR", "Diretor snapshot");
        UUID snapshotId = salvarSnapshot(publicoId, "dash-snap-matriculas-pendentes", "Matrículas pendentes", "12.00")
                .andExpect(jsonPath("$.publicoDashboardId").value(publicoId.toString()))
                .andExpect(jsonPath("$.publicoCodigo").value("DASH-SNAP-DIRETOR"))
                .andExpect(jsonPath("$.codigoIndicador").value("DASH-SNAP-MATRICULAS-PENDENTES"))
                .andExpect(jsonPath("$.valorNumeric").value(12.00))
                .andReturnId();

        salvarSnapshot(publicoId, "dash-snap-matriculas-pendentes", "Matrículas pendentes atualizadas", "15.00")
                .andExpect(jsonPath("$.id").value(snapshotId.toString()))
                .andExpect(jsonPath("$.descricao").value("Matrículas pendentes atualizadas"))
                .andExpect(jsonPath("$.valorNumeric").value(15.00));

        mockMvc.perform(get("/api/dashboard/snapshots")
                        .param("publicoDashboardId", publicoId.toString())
                        .param("referenciaData", "2055-04-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigoIndicador == 'DASH-SNAP-MATRICULAS-PENDENTES')].valorNumeric")
                        .value(hasItem(15.00)));

        mockMvc.perform(get("/api/dashboard/snapshots/publicos/{publicoCodigo}", "dash-snap-diretor")
                        .param("referenciaData", "2055-04-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigoIndicador == 'DASH-SNAP-MATRICULAS-PENDENTES')].descricao")
                        .value(hasItem("Matrículas pendentes atualizadas")));

        mockMvc.perform(delete("/api/dashboard/snapshots/{id}", snapshotId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/dashboard/snapshots")
                        .param("publicoDashboardId", publicoId.toString())
                        .param("referenciaData", "2055-04-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoParaPublicoInexistente() throws Exception {
        String requestBody = """
                {
                  "publicoDashboardId": "%s",
                  "codigoIndicador": "dash-snap-inexistente",
                  "descricao": "Indicador inexistente",
                  "valorNumeric": 1,
                  "referenciaData": "2055-04-10"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(put("/api/dashboard/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    private UUID criarPublico(String codigo, String descricao) {
        UUID publicoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO publico_dashboard (id_publico_dashboard, codigo, descricao)
                VALUES (?, ?, ?)
                """, publicoId, codigo, descricao);
        return publicoId;
    }

    private SnapshotResultActions salvarSnapshot(
            UUID publicoId,
            String codigoIndicador,
            String descricao,
            String valorNumeric) throws Exception {
        String requestBody = """
                {
                  "publicoDashboardId": "%s",
                  "codigoIndicador": "%s",
                  "descricao": "%s",
                  "valorNumeric": %s,
                  "valorTexto": "valor textual",
                  "referenciaData": "2055-04-10"
                }
                """.formatted(publicoId, codigoIndicador, descricao, valorNumeric);

        return new SnapshotResultActions(mockMvc.perform(put("/api/dashboard/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk()));
    }

    private final class SnapshotResultActions {

        private final org.springframework.test.web.servlet.ResultActions delegate;

        private SnapshotResultActions(org.springframework.test.web.servlet.ResultActions delegate) {
            this.delegate = delegate;
        }

        private SnapshotResultActions andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            delegate.andExpect(matcher);
            return this;
        }

        private UUID andReturnId() throws Exception {
            String responseBody = delegate.andReturn().getResponse().getContentAsString();
            return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
        }
    }
}
