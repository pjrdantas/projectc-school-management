package br.com.escola.dashboard.adapter.in.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
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
                "DELETE FROM dashboard_indicador_snapshot WHERE codigo_indicador LIKE 'PROFESSOR_%'",
                "DELETE FROM publico_dashboard WHERE codigo LIKE 'DASH-SNAP-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM dashboard_indicador_snapshot WHERE codigo_indicador LIKE 'DASH-SNAP-%'",
                "DELETE FROM dashboard_indicador_snapshot WHERE codigo_indicador LIKE 'PROFESSOR_%'",
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

    @Test
    @WithMockUser
    void deveConsultarHistoricoComVariacaoPorIndicador() throws Exception {
        UUID publicoId = criarPublico("DASH-SNAP-HIST", "Histórico snapshot");
        salvarSnapshot(publicoId, "dash-snap-total-matriculas", "Total de matrículas", "10.00", null, "2055-04-01");
        salvarSnapshot(publicoId, "dash-snap-total-matriculas", "Total de matrículas", "15.00", null, "2055-04-10");
        salvarSnapshot(publicoId, "dash-snap-total-matriculas", "Total de matrículas", "18.00", null, "2055-04-20");
        salvarSnapshot(publicoId, "dash-snap-outro", "Outro indicador", "99.00", null, "2055-04-10");

        mockMvc.perform(get("/api/dashboard/snapshots/historico/publicos/{publicoCodigo}", "dash-snap-hist")
                        .param("codigoIndicador", "dash-snap-total-matriculas")
                        .param("dataInicio", "2055-04-01")
                        .param("dataFim", "2055-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].publicoCodigo").value("DASH-SNAP-HIST"))
                .andExpect(jsonPath("$[0].codigoIndicador").value("DASH-SNAP-TOTAL-MATRICULAS"))
                .andExpect(jsonPath("$[0].valorAtual").value(18.00))
                .andExpect(jsonPath("$[0].valorAnterior").value(15.00))
                .andExpect(jsonPath("$[0].variacaoPercentual").value(20.00))
                .andExpect(jsonPath("$[0].pontos", hasSize(3)))
                .andExpect(jsonPath("$[0].pontos[0].referenciaData").value("2055-04-01"))
                .andExpect(jsonPath("$[0].pontos[2].referenciaData").value("2055-04-20"));
    }

    @Test
    @WithMockUser
    void deveConsultarHistoricoDoProfessorPorIndicadorEscopado() throws Exception {
        UUID publicoId = criarPublico("DASH-SNAP-PROFESSOR", "Professor snapshot");
        UUID professorId = UUID.randomUUID();
        UUID outroProfessorId = UUID.randomUUID();
        String prefixoProfessor = "PROFESSOR_" + professorId.toString().replace("-", "").toUpperCase();
        String prefixoOutroProfessor = "PROFESSOR_" + outroProfessorId.toString().replace("-", "").toUpperCase();
        salvarSnapshot(publicoId, prefixoProfessor + "_AULAS_REALIZADAS", "Aulas realizadas", "4.00", professorId.toString(), "2055-04-01");
        salvarSnapshot(publicoId, prefixoProfessor + "_AULAS_REALIZADAS", "Aulas realizadas", "6.00", professorId.toString(), "2055-04-10");
        salvarSnapshot(publicoId, prefixoOutroProfessor + "_AULAS_REALIZADAS", "Aulas realizadas", "30.00", outroProfessorId.toString(), "2055-04-10");

        mockMvc.perform(get("/api/dashboard/snapshots/historico/publicos/{publicoCodigo}", "dash-snap-professor")
                        .param("professorId", professorId.toString())
                        .param("codigoIndicador", "aulas_realizadas")
                        .param("dataInicio", "2055-04-01")
                        .param("dataFim", "2055-04-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoIndicador").value(prefixoProfessor + "_AULAS_REALIZADAS"))
                .andExpect(jsonPath("$[0].valorAtual").value(6.00))
                .andExpect(jsonPath("$[0].valorAnterior").value(4.00))
                .andExpect(jsonPath("$[0].variacaoPercentual").value(50.00))
                .andExpect(jsonPath("$[0].pontos", hasSize(2)))
                .andExpect(jsonPath("$[0].pontos[*].valorTexto").value(hasItem(professorId.toString())));
    }

    @Test
    @WithMockUser
    void deveRejeitarHistoricoComPeriodoInvertido() throws Exception {
        criarPublico("DASH-SNAP-HIST-INVALIDO", "Histórico inválido");

        mockMvc.perform(get("/api/dashboard/snapshots/historico/publicos/{publicoCodigo}", "dash-snap-hist-invalido")
                        .param("dataInicio", "2055-04-20")
                        .param("dataFim", "2055-04-01"))
                .andExpect(status().isBadRequest());
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

    private void salvarSnapshot(
            UUID publicoId,
            String codigoIndicador,
            String descricao,
            String valorNumeric,
            String valorTexto,
            String referenciaData) {
        jdbcTemplate.update("""
                INSERT INTO dashboard_indicador_snapshot (
                    id_dashboard_indicador_snapshot, id_publico_dashboard, codigo_indicador,
                    descricao, valor_numeric, valor_texto, referencia_data, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                UUID.randomUUID(),
                publicoId,
                codigoIndicador.toUpperCase(),
                descricao,
                new java.math.BigDecimal(valorNumeric),
                valorTexto,
                java.sql.Date.valueOf(referenciaData));
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
