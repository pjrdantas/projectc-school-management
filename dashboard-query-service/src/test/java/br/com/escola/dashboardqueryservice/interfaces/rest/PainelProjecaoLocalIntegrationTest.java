package br.com.escola.dashboardqueryservice.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "dashboard-query.internal-api.token=dashboard-token",
        "spring.datasource.url=jdbc:h2:mem:dashboard-query;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
class PainelProjecaoLocalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveServirResumosSomentePeloReadModelLocal() throws Exception {
        UUID escolaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();

        atualizar(escolaId, "ACADEMICO", """
                {"escolaId":"%s","escolaNome":"Escola Local","totalMatriculas":12,
                 "matriculasPorStatus":[],"turmasComVagas":[]}
                """.formatted(escolaId), "");
        atualizar(escolaId, "SECRETARIA", """
                {"escolaId":"%s","escolaNome":"Escola Local","matriculasEmAndamento":4,
                 "matriculasPorStatus":[],"turmasComVagas":[]}
                """.formatted(escolaId), "");
        atualizar(escolaId, "DIRETOR", """
                {"escolaId":"%s","escolaNome":"Escola Local","alunosAtivos":10,
                 "matriculasPorStatus":[],"turmasComVagas":[]}
                """.formatted(escolaId), "");
        atualizar(escolaId, "PROFESSOR", """
                {"escolaId":"%s","escolaNome":"Escola Local","professorId":"%s",
                 "aulasRealizadas":7,"turmas":[]}
                """.formatted(escolaId, professorId), ",\"professorId\":\"" + professorId + "\"");

        mockMvc.perform(leitura(get("/internal/v1/dashboard/academico"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaNome").value("Escola Local"))
                .andExpect(jsonPath("$.totalMatriculas").value(12));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/secretaria"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculasEmAndamento").value(4));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/diretor"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunosAtivos").value(10));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/professores/{id}", professorId), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aulasRealizadas").value(7));
    }

    @Test
    void deveServirConsultasCompostasPelasProjecoesLocais() throws Exception {
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        UUID publicoId = UUID.randomUUID();

        atualizar(escolaId, "ALERTAS", """
                [{"publicoCodigo":"PROFESSOR","professorId":"%s","codigo":"FREQUENCIA_PENDENTE",
                  "severidade":"ALTA","titulo":"Pendencia","mensagem":"Registrar frequencia","valor":2,"limite":0}]
                """.formatted(professorId), ",\"publicoCodigo\":\"PROFESSOR\",\"professorId\":\"" + professorId + "\"");
        atualizar(escolaId, "FRONTEND", """
                {"publicoCodigo":"PROFESSOR","usuarioId":"%s","professorId":"%s","resumo":{},
                 "alertas":[],"dashboards":[],"configuracoesUsuario":[],"historico":[]}
                """.formatted(usuarioId, professorId),
                ",\"publicoCodigo\":\"PROFESSOR\",\"professorId\":\"" + professorId
                        + "\",\"usuarioId\":\"" + usuarioId + "\"");
        atualizar(escolaId, "SNAPSHOTS", """
                [{"id":"%s","publicoPainelId":"%s","publicoCodigo":"DIRETOR","escolaId":"%s",
                  "escolaNome":"Escola Local","codigoIndicador":"TOTAL_MATRICULAS","descricao":"Total",
                  "valorNumeric":15,"valorTexto":"15","referenciaData":"2026-07-20"}]
                """.formatted(UUID.randomUUID(), publicoId, escolaId),
                ",\"publicoCodigo\":\"DIRETOR\",\"referenciaData\":\"2026-07-20\"");
        atualizar(escolaId, "HISTORICO", """
                [{"publicoCodigo":"DIRETOR","codigoIndicador":"TOTAL_MATRICULAS","descricao":"Total",
                  "valorAtual":15,"valorAnterior":10,"variacaoPercentual":50,
                  "pontos":[{"referenciaData":"2026-06-20","valorNumeric":10,"valorTexto":"10"},
                            {"referenciaData":"2026-07-20","valorNumeric":15,"valorTexto":"15"}]}]
                """, ",\"publicoCodigo\":\"DIRETOR\"");
        atualizar(escolaId, "PUBLICOS", """
                [{"id":"%s","codigo":"DIRETOR","descricao":"Diretoria"}]
                """.formatted(publicoId), "");
        atualizar(escolaId, "PAINEIS", """
                [{"id":"%s","publicoPainelId":"%s","publicoCodigo":"DIRETOR","codigo":"GERAL",
                  "nome":"Visao geral","descricao":"Resumo","ativo":true}]
                """.formatted(UUID.randomUUID(), publicoId), "");

        mockMvc.perform(leitura(get("/internal/v1/dashboard/alertas")
                        .param("publicoCodigo", "professor").param("professorId", professorId.toString()), escolaId))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigo").value("FREQUENCIA_PENDENTE"));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/frontend")
                        .param("publicoCodigo", "PROFESSOR").param("usuarioId", usuarioId.toString())
                        .param("professorId", professorId.toString()), escolaId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/snapshots/publicos/DIRETOR")
                        .param("referenciaData", "2026-07-20"), escolaId))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].valorNumeric").value(15));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/snapshots/historico/publicos/DIRETOR")
                        .param("codigoIndicador", "TOTAL_MATRICULAS").param("dataInicio", "2026-07-01"), escolaId))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].pontos.length()").value(1));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/configuracoes/publicos"), escolaId))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigo").value("DIRETOR"));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/configuracoes/dashboards")
                        .param("publicoCodigo", "diretor"), escolaId))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigo").value("GERAL"));
    }

    @Test
    void deveAtualizarIdempotentementeEIsolarProjecoesPorEscola() throws Exception {
        UUID escolaId = UUID.randomUUID();
        atualizar(escolaId, "ACADEMICO", """
                {"escolaId":"%s","escolaNome":"Primeira","totalMatriculas":1,
                 "matriculasPorStatus":[],"turmasComVagas":[]}
                """.formatted(escolaId), "");
        atualizar(escolaId, "ACADEMICO", """
                {"escolaId":"%s","escolaNome":"Atualizada","totalMatriculas":2,
                 "matriculasPorStatus":[],"turmasComVagas":[]}
                """.formatted(escolaId), "");

        mockMvc.perform(leitura(get("/internal/v1/dashboard/academico"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaNome").value("Atualizada"))
                .andExpect(jsonPath("$.totalMatriculas").value(2));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/academico"), UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void deveRejeitarTokenOuPayloadInvalido() throws Exception {
        UUID escolaId = UUID.randomUUID();
        mockMvc.perform(get("/internal/v1/dashboard/academico")
                        .header("X-Internal-Token", "invalido")
                        .header("X-Correlation-Id", "corr-invalid")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer internal"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(escrita(escolaId)
                        .content("{\"tipo\":\"PROFESSOR\",\"payload\":{}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void deveAdministrarPublicoEPainelComIsolamentoEInvariantes() throws Exception {
        UUID escolaId = UUID.randomUUID();
        MvcResult publicoResultado = mockMvc.perform(interno(post("/internal/v1/dashboard/configuracoes/publicos"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"diretor\",\"descricao\":\"Diretoria\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("DIRETOR"))
                .andReturn();
        UUID publicoId = UUID.fromString(new ObjectMapper().readTree(publicoResultado.getResponse().getContentAsString())
                .get("id").asText());
        mockMvc.perform(interno(put("/internal/v1/dashboard/configuracoes/publicos/{id}", publicoId), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"diretor\",\"descricao\":\"Diretoria atualizada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Diretoria atualizada"));

        MvcResult painelResultado = mockMvc.perform(interno(post("/internal/v1/dashboard/configuracoes/dashboards"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"publicoId":"%s","codigo":"geral","nome":"Visao geral","ativo":true}
                                """.formatted(publicoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("GERAL"))
                .andReturn();
        UUID painelId = UUID.fromString(new ObjectMapper().readTree(painelResultado.getResponse().getContentAsString())
                .get("id").asText());
        mockMvc.perform(interno(put("/internal/v1/dashboard/configuracoes/dashboards/{id}", painelId), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"publicoId":"%s","codigo":"geral","nome":"Visao atualizada","ativo":false}
                                """.formatted(publicoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Visao atualizada"))
                .andExpect(jsonPath("$.ativo").value(false));

        MvcResult widgetResultado = mockMvc.perform(interno(post("/internal/v1/dashboard/configuracoes/widgets"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"painelId":"%s","codigo":"matriculas","titulo":"Matriculas",
                                 "tipoWidget":"indicador","ordem":0,"ativo":true}
                                """.formatted(painelId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("MATRICULAS"))
                .andReturn();
        UUID widgetId = UUID.fromString(new ObjectMapper().readTree(widgetResultado.getResponse().getContentAsString())
                .get("id").asText());
        mockMvc.perform(interno(get("/internal/v1/dashboard/configuracoes/dashboards/{id}/widgets", painelId), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(widgetId.toString()));
        mockMvc.perform(interno(post("/internal/v1/dashboard/configuracoes/widgets"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"painelId":"%s","codigo":"outro","titulo":"Outro",
                                 "tipoWidget":"INDICADOR","ordem":0}
                                """.formatted(painelId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_CONFLICT"));
        mockMvc.perform(interno(put("/internal/v1/dashboard/configuracoes/widgets/{id}", widgetId), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"painelId":"%s","codigo":"matriculas","titulo":"Matriculas atualizadas",
                                 "tipoWidget":"INDICADOR","ordem":1,"ativo":false}
                                """.formatted(painelId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordem").value(1))
                .andExpect(jsonPath("$.ativo").value(false));

        UUID usuarioId = UUID.randomUUID();
        mockMvc.perform(interno(put("/internal/v1/dashboard/usuarios/{usuarioId}/widgets/{widgetId}/configuracao", usuarioId, widgetId),
                        escolaId, usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visivel\":false,\"ordem\":2,\"configuracaoJson\":\"{\\\"colunas\\\":2}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.configuracaoJson").value("{\"colunas\":2}"));
        mockMvc.perform(interno(get("/internal/v1/dashboard/usuarios/{usuarioId}/configuracoes", usuarioId)
                        .param("painelId", painelId.toString()), escolaId, usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].widgetId").value(widgetId.toString()));
        mockMvc.perform(interno(put("/internal/v1/dashboard/usuarios/{usuarioId}/widgets/{widgetId}/configuracao",
                        UUID.randomUUID(), widgetId), escolaId, usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));

        mockMvc.perform(interno(delete("/internal/v1/dashboard/configuracoes/publicos/{id}", publicoId), escolaId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_CONFLICT"));
        mockMvc.perform(interno(delete("/internal/v1/dashboard/configuracoes/dashboards/{id}", painelId), escolaId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_CONFLICT"));
        mockMvc.perform(interno(delete("/internal/v1/dashboard/configuracoes/widgets/{id}", widgetId), escolaId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_CONFLICT"));
        mockMvc.perform(interno(delete("/internal/v1/dashboard/usuarios/{usuarioId}/widgets/{widgetId}/configuracao", usuarioId, widgetId),
                        escolaId, usuarioId))
                .andExpect(status().isNoContent());
        mockMvc.perform(interno(delete("/internal/v1/dashboard/configuracoes/widgets/{id}", widgetId), escolaId))
                .andExpect(status().isNoContent());
        mockMvc.perform(interno(delete("/internal/v1/dashboard/configuracoes/dashboards/{id}", painelId), escolaId))
                .andExpect(status().isNoContent());
        mockMvc.perform(interno(delete("/internal/v1/dashboard/configuracoes/publicos/{id}", publicoId), escolaId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveManterSnapshotLocalIdempotenteEHistoricoDerivado() throws Exception {
        UUID escolaId = UUID.randomUUID();
        MvcResult publicoResultado = mockMvc.perform(interno(post("/internal/v1/dashboard/configuracoes/publicos"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"academico\",\"descricao\":\"Academico\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID publicoId = UUID.fromString(new ObjectMapper().readTree(publicoResultado.getResponse().getContentAsString())
                .get("id").asText());

        salvarSnapshot(escolaId, publicoId, "2026-01-01", 10);
        salvarSnapshot(escolaId, publicoId, "2026-02-01", 15);
        salvarSnapshot(escolaId, publicoId, "2026-02-01", 16);

        mockMvc.perform(interno(get("/internal/v1/dashboard/snapshots/locais")
                        .param("publicoId", publicoId.toString()).param("referenciaData", "2026-02-01"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valorNumeric").value(16));
        mockMvc.perform(interno(get("/internal/v1/dashboard/snapshots/locais/historico/publicos/ACADEMICO"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valorAtual").value(16))
                .andExpect(jsonPath("$[0].valorAnterior").value(10))
                .andExpect(jsonPath("$[0].variacaoPercentual").value(60))
                .andExpect(jsonPath("$[0].pontos.length()").value(2));
    }

    @Test
    void devePublicarIndicadoresDaOrigemPermitidaEAlimentarProjecoesLocais() throws Exception {
        UUID escolaId = UUID.randomUUID();
        MvcResult publicoResultado = mockMvc.perform(interno(post("/internal/v1/dashboard/configuracoes/publicos"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"academico\",\"descricao\":\"Academico\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID publicoId = UUID.fromString(new ObjectMapper().readTree(publicoResultado.getResponse().getContentAsString())
                .get("id").asText());

        mockMvc.perform(interno(put("/internal/v1/dashboard/indicadores/publicacoes"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"origem":"MATRICULA_DOCUMENTO","publicoId":"%s","referenciaData":"2026-03-01",
                                 "escolaNome":"Escola Local","indicadores":[{"codigoIndicador":"total_matriculas",
                                 "descricao":"Total de matriculas","valorNumeric":20}]}
                                """.formatted(publicoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoIndicador").value("TOTAL_MATRICULAS"))
                .andExpect(jsonPath("$[0].valorNumeric").value(20));

        mockMvc.perform(leitura(get("/internal/v1/dashboard/snapshots/publicos/ACADEMICO")
                        .param("referenciaData", "2026-03-01"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valorNumeric").value(20));
        mockMvc.perform(leitura(get("/internal/v1/dashboard/snapshots/historico/publicos/ACADEMICO"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valorAtual").value(20));
        mockMvc.perform(interno(put("/internal/v1/dashboard/indicadores/publicacoes"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"origem":"PESSOAS","publicoId":"%s","referenciaData":"2026-03-01",
                                 "indicadores":[{"codigoIndicador":"total_matriculas","descricao":"Invalido","valorNumeric":1}]}
                                """.formatted(publicoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void deveServirCompatibilidadeDasGeracoesComSnapshotsLocaisPublicados() throws Exception {
        UUID escolaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        UUID publicoAcademicoId = criarPublico(escolaId, "academico");
        UUID publicoProfessorId = criarPublico(escolaId, "professor");
        String prefixoProfessor = "PROFESSOR_" + professorId.toString().replace("-", "").toUpperCase() + "_";

        salvarSnapshot(escolaId, publicoAcademicoId, "2026-07-23", 12);
        mockMvc.perform(interno(put("/internal/v1/dashboard/snapshots/locais"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"publicoId":"%s","codigoIndicador":"%sAULAS_REALIZADAS","descricao":"Aulas",
                                 "valorNumeric":7,"escolaNome":"Escola Local","referenciaData":"2026-07-23"}
                                """.formatted(publicoProfessorId, prefixoProfessor)))
                .andExpect(status().isOk());

        mockMvc.perform(interno(post("/internal/v1/dashboard/snapshots/geracoes/academico")
                        .param("referenciaData", "2026-07-23"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoIndicador").value("TOTAL_MATRICULAS"));
        mockMvc.perform(interno(post("/internal/v1/dashboard/snapshots/geracoes/professores/{professorId}", professorId)
                        .param("referenciaData", "2026-07-23"), escolaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoIndicador").value(prefixoProfessor + "AULAS_REALIZADAS"));
    }

    private void atualizar(UUID escolaId, String tipo, String payload, String metadados) throws Exception {
        mockMvc.perform(escrita(escolaId).content("""
                {"tipo":"%s"%s,"payload":%s}
                """.formatted(tipo, metadados, payload)))
                .andExpect(status().isNoContent());
    }

    private MockHttpServletRequestBuilder leitura(MockHttpServletRequestBuilder request, UUID escolaId) {
        return request
                .header("X-Internal-Token", "dashboard-token")
                .header("X-Correlation-Id", "corr-" + UUID.randomUUID())
                .header("X-Usuario-Id", UUID.randomUUID())
                .header("X-Escola-Id", escolaId)
                .header("Authorization", "Bearer internal");
    }

    private MockHttpServletRequestBuilder escrita(UUID escolaId) {
        return put("/internal/v1/dashboard/projecoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Token", "dashboard-token")
                .header("X-Correlation-Id", "corr-" + UUID.randomUUID())
                .header("X-Usuario-Id", UUID.randomUUID())
                .header("X-Escola-Id", escolaId);
    }

    private void salvarSnapshot(UUID escolaId, UUID publicoId, String referenciaData, int valor) throws Exception {
        mockMvc.perform(interno(put("/internal/v1/dashboard/snapshots/locais"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"publicoId":"%s","codigoIndicador":"total_matriculas","descricao":"Total",
                                 "valorNumeric":%s,"escolaNome":"Escola Local","referenciaData":"%s"}
                                """.formatted(publicoId, valor, referenciaData)))
                .andExpect(status().isOk());
    }

    private UUID criarPublico(UUID escolaId, String codigo) throws Exception {
        MvcResult resultado = mockMvc.perform(interno(post("/internal/v1/dashboard/configuracoes/publicos"), escolaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"descricao\":\"" + codigo + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(new ObjectMapper().readTree(resultado.getResponse().getContentAsString()).get("id").asText());
    }

    private MockHttpServletRequestBuilder interno(MockHttpServletRequestBuilder request, UUID escolaId) {
        return interno(request, escolaId, UUID.randomUUID());
    }

    private MockHttpServletRequestBuilder interno(
            MockHttpServletRequestBuilder request, UUID escolaId, UUID usuarioId) {
        return request
                .header("X-Internal-Token", "dashboard-token")
                .header("X-Correlation-Id", "corr-" + UUID.randomUUID())
                .header("X-Usuario-Id", usuarioId)
                .header("X-Escola-Id", escolaId);
    }
}
