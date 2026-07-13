package br.com.escola.pedagogicalservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest
@AutoConfigureMockMvc
class PedagogicalInternalControllerIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("pedagogical.internal-api.token", () -> "pedagogical-token");
        registry.add("pedagogical.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveConsultarBoletimNoContratoInterno() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID boletimId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "boletimId":"%s",
                          "matriculaId":"%s",
                          "alunoId":"%s",
                          "alunoNome":"Aluno Pedagogico",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "turmaNome":"6A",
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000091",
                          "periodoLetivoNome":"2026",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "dataGeracao":"2026-07-12",
                          "periodoReferencia":"2BIM",
                          "dataFechamento":"2026-07-12",
                          "observacao":"Boletim integrado",
                          "persistido":true,
                          "indicadores":{
                            "totalDisciplinas":1,
                            "mediaGeral":8.50,
                            "frequenciaGeralPercentual":95.00,
                            "resultadoGeral":"APROVADO"
                          },
                          "itens":[
                            {
                              "disciplinaId":"%s",
                              "disciplinaNome":"Matematica",
                              "media":8.50,
                              "frequenciaPercentual":95.00,
                              "totalAvaliacoes":4,
                              "totalFrequencias":20,
                              "resultado":"APROVADO"
                            }
                          ]
                        }
                        """.formatted(boletimId, matriculaId, alunoId, disciplinaId)));

        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}/boletim", matriculaId)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Pedagogico"))
                .andExpect(jsonPath("$.itens[0].disciplinaNome").value("Matematica"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/boletins/matriculas/" + matriculaId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer pedagogical-user-token");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-1");
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}/boletim", UUID.randomUUID())
                        .header("X-Correlation-Id", "corr-pedagogical-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveListarFechamentosNoContratoInterno() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID boletimId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "boletimId":"%s",
                            "matriculaId":"%s",
                            "alunoId":"00000000-0000-0000-0000-000000000021",
                            "alunoNome":"Aluno Pedagogico",
                            "turmaId":"00000000-0000-0000-0000-000000000071",
                            "turmaNome":"6A",
                            "periodoLetivoId":"00000000-0000-0000-0000-000000000091",
                            "periodoLetivoNome":"2026",
                            "escolaId":"00000000-0000-0000-0000-000000000047",
                            "escolaNome":"Escola padrao",
                            "dataGeracao":"2026-07-12",
                            "periodoReferencia":"1BIM",
                            "dataFechamento":"2026-07-10",
                            "observacao":"Fechamento integrado",
                            "persistido":true,
                            "indicadores":{
                              "totalDisciplinas":1,
                              "mediaGeral":8.50,
                              "frequenciaGeralPercentual":95.00,
                              "resultadoGeral":"APROVADO"
                            },
                            "itens":[]
                          }
                        ]
                        """.formatted(boletimId, matriculaId)));

        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}/boletim/fechamentos", matriculaId)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].periodoReferencia").value("1BIM"))
                .andExpect(jsonPath("$[0].persistido").value(true));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/boletins/matriculas/" + matriculaId + "/fechamentos");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer pedagogical-user-token");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-3");
    }

    @Test
    void deveCarregarHistoricoNovoNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "contexto":{
                            "idHistoricoEscolar":null,
                            "idAluno":"%s",
                            "idMatricula":"%s",
                            "modo":"CADASTRO",
                            "status":"RASCUNHO",
                            "serieMatriculaAtual":6,
                            "serieConcluidaOrigem":5,
                            "escolaOrigem":"Escola Origem",
                            "dataTransferencia":"2026-07-12",
                            "bloqueado":false
                          },
                          "cabecalho":null,
                          "aluno":null,
                          "periodos":[],
                          "baseComum":[],
                          "parteDiversificada":[],
                          "totais":null,
                          "estudosRealizados":[],
                          "observacoes":"Observacoes",
                          "certificado":null,
                          "pendencias":[]
                        }
                        """.formatted(alunoId, matriculaId)));

        mockMvc.perform(get("/internal/v1/historicos-escolares/novo")
                        .param("idAluno", alunoId.toString())
                        .param("idMatricula", matriculaId.toString())
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.idAluno").value(alunoId.toString()))
                .andExpect(jsonPath("$.contexto.idMatricula").value(matriculaId.toString()))
                .andExpect(jsonPath("$.contexto.modo").value("CADASTRO"));

        RecordedRequest recorded = aguardarRequisicao(
                "GET",
                "/internal/historicos-escolares/novo?idAluno=" + alunoId + "&idMatricula=" + matriculaId + "&modo=CADASTRO");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer pedagogical-user-token");
    }

    @Test
    void deveCarregarHistoricoParaEdicaoNoContratoInterno() throws Exception {
        UUID historicoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "contexto":{
                            "idHistoricoEscolar":"%s",
                            "idAluno":"00000000-0000-0000-0000-000000000021",
                            "idMatricula":"00000000-0000-0000-0000-000000000031",
                            "modo":"EDICAO",
                            "status":"RASCUNHO",
                            "serieMatriculaAtual":6,
                            "serieConcluidaOrigem":5,
                            "escolaOrigem":"Escola Origem",
                            "dataTransferencia":"2026-07-12",
                            "bloqueado":false
                          },
                          "cabecalho":null,
                          "aluno":null,
                          "periodos":[],
                          "baseComum":[],
                          "parteDiversificada":[],
                          "totais":null,
                          "estudosRealizados":[],
                          "observacoes":"Observacoes",
                          "certificado":null,
                          "pendencias":[]
                        }
                        """.formatted(historicoId)));

        mockMvc.perform(get("/internal/v1/historicos-escolares/{id}/carregamento", historicoId)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-5")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.idHistoricoEscolar").value(historicoId.toString()))
                .andExpect(jsonPath("$.contexto.modo").value("EDICAO"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/historicos-escolares/" + historicoId + "/carregamento");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-5");
    }

    private RecordedRequest aguardarRequisicao(String method, String path) throws InterruptedException {
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getMethod()).isEqualTo(method);
        assertThat(recorded.getPath()).isEqualTo(path);
        return recorded;
    }
}
