package br.com.escola.dashboardqueryservice.interfaces.rest;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
}
