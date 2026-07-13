package br.com.escola.pedagogicalservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void deveCriarAulaNoContratoInterno() throws Exception {
        UUID alocacaoId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();
        String requestBody = """
                {
                  "professorTurmaDisciplinaId":"%s",
                  "dataAula":"2038-03-10",
                  "realizada":true
                }
                """.formatted(alocacaoId);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "professorTurmaDisciplinaId":"%s",
                          "professorId":"00000000-0000-0000-0000-000000000101",
                          "professorNome":"Professor Aula",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "turmaNome":"Turma Aula",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "disciplinaId":"00000000-0000-0000-0000-000000000081",
                          "disciplinaNome":"Matematica",
                          "dataAula":"2038-03-10",
                          "horarioInicio":"07:30:00",
                          "horarioFim":"08:20:00",
                          "conteudoMinistrado":"Conteudo",
                          "observacao":"Observacao",
                          "realizada":true,
                          "createdAt":"2038-03-10T07:00:00"
                        }
                        """.formatted(aulaId, alocacaoId)));

        mockMvc.perform(post("/internal/v1/aulas")
                        .contentType("application/json")
                        .content(requestBody)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-aula-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(aulaId.toString()))
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Aula"));

        RecordedRequest recorded = aguardarRequisicao("POST", "/internal/aulas");
        assertThat(recorded.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveCriarAvaliacaoNoContratoInterno() throws Exception {
        UUID alocacaoId = UUID.randomUUID();
        UUID avaliacaoId = UUID.randomUUID();
        String requestBody = """
                {
                  "professorTurmaDisciplinaId":"%s",
                  "titulo":"Prova fase 130",
                  "descricao":"Avaliacao integrada",
                  "dataAplicacao":"2039-04-15",
                  "valorMaximo":10.00,
                  "peso":1.00,
                  "tipoAvaliacao":"PROVA"
                }
                """.formatted(alocacaoId);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "professorTurmaDisciplinaId":"%s",
                          "professorId":"00000000-0000-0000-0000-000000000101",
                          "professorNome":"Professor Avaliacao",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "turmaNome":"Turma Avaliacao",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "disciplinaId":"00000000-0000-0000-0000-000000000081",
                          "disciplinaNome":"Matematica",
                          "titulo":"Prova fase 130",
                          "descricao":"Avaliacao integrada",
                          "dataAplicacao":"2039-04-15",
                          "valorMaximo":10.00,
                          "peso":1.00,
                          "tipoAvaliacao":"PROVA",
                          "createdAt":"2039-04-15T07:00:00"
                        }
                        """.formatted(avaliacaoId, alocacaoId)));

        mockMvc.perform(post("/internal/v1/avaliacoes")
                        .contentType("application/json")
                        .content(requestBody)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-avaliacao-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Avaliacao"));

        RecordedRequest recorded = aguardarRequisicao("POST", "/internal/avaliacoes");
        assertThat(recorded.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveListarAvaliacoesNoContratoInterno() throws Exception {
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID avaliacaoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "professorTurmaDisciplinaId":"%s",
                            "professorId":"00000000-0000-0000-0000-000000000101",
                            "professorNome":"Professor Avaliacao",
                            "turmaId":"%s",
                            "turmaNome":"Turma Avaliacao",
                            "escolaId":"00000000-0000-0000-0000-000000000047",
                            "escolaNome":"Escola padrao",
                            "disciplinaId":"00000000-0000-0000-0000-000000000081",
                            "disciplinaNome":"Matematica",
                            "titulo":"Prova fase 130",
                            "descricao":"Avaliacao integrada",
                            "dataAplicacao":"2039-04-15",
                            "valorMaximo":10.00,
                            "peso":1.00,
                            "tipoAvaliacao":"PROVA",
                            "createdAt":"2039-04-15T07:00:00"
                          }
                        ]
                        """.formatted(avaliacaoId, alocacaoId, turmaId)));

        mockMvc.perform(get("/internal/v1/avaliacoes")
                        .param("professorTurmaDisciplinaId", alocacaoId.toString())
                        .param("turmaId", turmaId.toString())
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-avaliacao-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$[0].professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()));

        RecordedRequest recorded = aguardarRequisicao(
                "GET",
                "/internal/avaliacoes?professorTurmaDisciplinaId=" + alocacaoId + "&turmaId=" + turmaId);
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-avaliacao-2");
    }

    @Test
    void deveBuscarAvaliacaoPorIdNoContratoInterno() throws Exception {
        UUID avaliacaoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "professorTurmaDisciplinaId":"00000000-0000-0000-0000-000000000111",
                          "professorId":"00000000-0000-0000-0000-000000000101",
                          "professorNome":"Professor Avaliacao",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "turmaNome":"Turma Avaliacao",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "disciplinaId":"00000000-0000-0000-0000-000000000081",
                          "disciplinaNome":"Matematica",
                          "titulo":"Prova fase 130",
                          "descricao":"Avaliacao integrada",
                          "dataAplicacao":"2039-04-15",
                          "valorMaximo":10.00,
                          "peso":1.00,
                          "tipoAvaliacao":"PROVA",
                          "createdAt":"2039-04-15T07:00:00"
                        }
                        """.formatted(avaliacaoId)));

        mockMvc.perform(get("/internal/v1/avaliacoes/{id}", avaliacaoId)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-avaliacao-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Avaliacao"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/avaliacoes/" + avaliacaoId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer pedagogical-user-token");
    }

    @Test
    void deveLancarNotaNoContratoInterno() throws Exception {
        UUID avaliacaoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();
        String requestBody = """
                {
                  "matriculaId":"%s",
                  "nota":8.50,
                  "observacao":"Boa participacao"
                }
                """.formatted(matriculaId);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "avaliacaoId":"%s",
                          "avaliacaoTitulo":"Prova fase 131",
                          "matriculaId":"%s",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "alunoNome":"Aluno Nota",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "nota":8.50,
                          "observacao":"Boa participacao",
                          "createdAt":"2039-04-15T08:00:00",
                          "updatedAt":"2039-04-15T08:05:00"
                        }
                        """.formatted(UUID.randomUUID(), avaliacaoId, matriculaId)));

        mockMvc.perform(post("/internal/v1/avaliacoes/{id}/notas", avaliacaoId)
                        .contentType("application/json")
                        .content(requestBody)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-nota-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.avaliacaoId").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Nota"));

        RecordedRequest recorded = aguardarRequisicao("POST", "/internal/avaliacoes/" + avaliacaoId + "/notas");
        assertThat(recorded.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveListarNotasPorAvaliacaoNoContratoInterno() throws Exception {
        UUID avaliacaoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "avaliacaoId":"%s",
                            "avaliacaoTitulo":"Prova fase 131",
                            "matriculaId":"%s",
                            "alunoId":"00000000-0000-0000-0000-000000000021",
                            "alunoNome":"Aluno Nota",
                            "escolaId":"00000000-0000-0000-0000-000000000047",
                            "escolaNome":"Escola padrao",
                            "nota":8.50,
                            "observacao":"Boa participacao",
                            "createdAt":"2039-04-15T08:00:00",
                            "updatedAt":"2039-04-15T08:05:00"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), avaliacaoId, matriculaId)));

        mockMvc.perform(get("/internal/v1/avaliacoes/{id}/notas", avaliacaoId)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-nota-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].avaliacaoId").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/avaliacoes/" + avaliacaoId + "/notas");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer pedagogical-user-token");
    }

    @Test
    void deveListarNotasPorMatriculaNoContratoInterno() throws Exception {
        UUID matriculaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "avaliacaoId":"%s",
                            "avaliacaoTitulo":"Prova fase 131",
                            "matriculaId":"%s",
                            "alunoId":"00000000-0000-0000-0000-000000000021",
                            "alunoNome":"Aluno Matricula Nota",
                            "escolaId":"00000000-0000-0000-0000-000000000047",
                            "escolaNome":"Escola padrao",
                            "nota":9.00,
                            "observacao":"Otimo desempenho",
                            "createdAt":"2039-04-15T08:00:00",
                            "updatedAt":"2039-04-15T08:05:00"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID(), matriculaId)));

        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}/notas", matriculaId)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-nota-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].alunoNome").value("Aluno Matricula Nota"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/matriculas/" + matriculaId + "/notas");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-nota-3");
    }

    @Test
    void deveListarAulasNoContratoInterno() throws Exception {
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "professorTurmaDisciplinaId":"%s",
                            "professorId":"00000000-0000-0000-0000-000000000101",
                            "professorNome":"Professor Aula",
                            "turmaId":"%s",
                            "turmaNome":"Turma Aula",
                            "escolaId":"00000000-0000-0000-0000-000000000047",
                            "escolaNome":"Escola padrao",
                            "disciplinaId":"00000000-0000-0000-0000-000000000081",
                            "disciplinaNome":"Matematica",
                            "dataAula":"2038-03-10",
                            "horarioInicio":"07:30:00",
                            "horarioFim":"08:20:00",
                            "conteudoMinistrado":"Conteudo",
                            "observacao":"Observacao",
                            "realizada":true,
                            "createdAt":"2038-03-10T07:00:00"
                          }
                        ]
                        """.formatted(aulaId, alocacaoId, turmaId)));

        mockMvc.perform(get("/internal/v1/aulas")
                        .param("professorTurmaDisciplinaId", alocacaoId.toString())
                        .param("turmaId", turmaId.toString())
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-aula-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(aulaId.toString()))
                .andExpect(jsonPath("$[0].professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()));

        RecordedRequest recorded = aguardarRequisicao(
                "GET",
                "/internal/aulas?professorTurmaDisciplinaId=" + alocacaoId + "&turmaId=" + turmaId);
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-aula-2");
    }

    @Test
    void deveBuscarAulaPorIdNoContratoInterno() throws Exception {
        UUID aulaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "professorTurmaDisciplinaId":"00000000-0000-0000-0000-000000000111",
                          "professorId":"00000000-0000-0000-0000-000000000101",
                          "professorNome":"Professor Aula",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "turmaNome":"Turma Aula",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "disciplinaId":"00000000-0000-0000-0000-000000000081",
                          "disciplinaNome":"Matematica",
                          "dataAula":"2038-03-10",
                          "horarioInicio":"07:30:00",
                          "horarioFim":"08:20:00",
                          "conteudoMinistrado":"Conteudo",
                          "observacao":"Observacao",
                          "realizada":true,
                          "createdAt":"2038-03-10T07:00:00"
                        }
                        """.formatted(aulaId)));

        mockMvc.perform(get("/internal/v1/aulas/{id}", aulaId)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-aula-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aulaId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Aula"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/aulas/" + aulaId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer pedagogical-user-token");
    }

    @Test
    void deveCarregarDiarioClasseNoContratoInterno() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "cabecalho":{
                            "idProfessor":"%s",
                            "idTurma":"%s",
                            "idDisciplina":"%s",
                            "anoLetivo":2058,
                            "mes":6
                          },
                          "alunos":[{"nome":"Aluno Diario","frequencias":{"12":"P"}}],
                          "conteudosPlanejados":[{"periodo":"Aula 1","descricao":"Conteudo planejado"}],
                          "observacoes":["Observacao interna"],
                          "avaliacoes":[{"descricao":"Prova mensal","valor":"0 a 10"}],
                          "assinatura":{"nomeProfessor":"","dataAssinatura":""},
                          "bloqueado":false
                        }
                        """.formatted(professorId, turmaId, disciplinaId)));

        mockMvc.perform(get("/internal/v1/diarios-classe")
                        .param("idProfessor", professorId.toString())
                        .param("idTurma", turmaId.toString())
                        .param("idDisciplina", disciplinaId.toString())
                        .param("anoLetivo", "2058")
                        .param("mes", "6")
                        .param("dataReferencia", "2058-06-26")
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-diario-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cabecalho.idProfessor").value(professorId.toString()))
                .andExpect(jsonPath("$.cabecalho.idTurma").value(turmaId.toString()))
                .andExpect(jsonPath("$.cabecalho.idDisciplina").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.alunos[0].nome").value("Aluno Diario"))
                .andExpect(jsonPath("$.conteudosPlanejados[0].descricao").value("Conteudo planejado"))
                .andExpect(jsonPath("$.bloqueado").value(false));

        RecordedRequest recorded = aguardarRequisicao(
                "GET",
                "/internal/diarios-classe?idProfessor=" + professorId
                        + "&idTurma=" + turmaId
                        + "&idDisciplina=" + disciplinaId
                        + "&anoLetivo=2058&mes=6&dataReferencia=2058-06-26");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-diario-1");
    }

    @Test
    void deveSalvarDiarioClasseNoContratoInterno() throws Exception {
        String idDiarioClasse = "diario-2058-06-x";
        String requestBody = """
                {
                  "idDiarioClasse":"diario-2058-06-x",
                  "dataLancamento":"2058-06-26",
                  "frequencias":[{"idAluno":"00000000-0000-0000-0000-000000000021","situacao":"PRESENTE"}],
                  "conteudos":[{"idPlanejamentoAula":"00000000-0000-0000-0000-000000000031","descricao":"Conteudo ministrado"}],
                  "observacoes":["Observacao interna"],
                  "assinatura":{"nomeProfessor":"Professor Diario","dataAssinatura":"26/06/2058"}
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"idDiarioClasse":"diario-2058-06-x","status":"SALVO","mensagem":"Lancamento salvo com sucesso.","salvoEm":"2058-06-26T10:30:00","bloqueado":true}
                        """));

        mockMvc.perform(put("/internal/v1/diarios-classe/{idDiarioClasse}", idDiarioClasse)
                        .contentType("application/json")
                        .content(requestBody)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-diario-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDiarioClasse").value(idDiarioClasse))
                .andExpect(jsonPath("$.status").value("SALVO"))
                .andExpect(jsonPath("$.bloqueado").value(true));

        RecordedRequest recorded = aguardarRequisicao("PUT", "/internal/diarios-classe/" + idDiarioClasse);
        assertThat(recorded.getBody().readUtf8()).isEqualTo(requestBody);
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-diario-2");
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

    @Test
    void deveCriarHistoricoEscolarNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID historicoId = UUID.randomUUID();
        String requestBody = """
                {
                  "nomeAluno":"Aluno Pedagogico",
                  "alunoId":"%s",
                  "componentesCurriculares":[]
                }
                """.formatted(alunoId);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"id":"%s","alunoId":"%s","nomeAluno":"Aluno Pedagogico","componentesCurriculares":[]}
                        """.formatted(historicoId, alunoId)));

        mockMvc.perform(post("/internal/v1/historicos-escolares")
                        .contentType("application/json")
                        .content(requestBody)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-6")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(historicoId.toString()))
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()));

        RecordedRequest recorded = aguardarRequisicao("POST", "/internal/historicos-escolares");
        assertThat(recorded.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveAtualizarHistoricoEscolarNoContratoInterno() throws Exception {
        UUID historicoId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        String requestBody = """
                {
                  "nomeAluno":"Aluno Atualizado",
                  "alunoId":"%s",
                  "componentesCurriculares":[]
                }
                """.formatted(alunoId);

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"id":"%s","alunoId":"%s","nomeAluno":"Aluno Atualizado","componentesCurriculares":[]}
                        """.formatted(historicoId, alunoId)));

        mockMvc.perform(put("/internal/v1/historicos-escolares/{id}", historicoId)
                        .contentType("application/json")
                        .content(requestBody)
                        .header("X-Internal-Token", "pedagogical-token")
                        .header("X-Correlation-Id", "corr-pedagogical-7")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer pedagogical-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(historicoId.toString()))
                .andExpect(jsonPath("$.nomeAluno").value("Aluno Atualizado"));

        RecordedRequest recorded = aguardarRequisicao("PUT", "/internal/historicos-escolares/" + historicoId);
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-7");
        assertThat(recorded.getBody().readUtf8()).isEqualTo(requestBody);
    }

    private RecordedRequest aguardarRequisicao(String method, String path) throws InterruptedException {
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getMethod()).isEqualTo(method);
        assertThat(recorded.getPath()).isEqualTo(path);
        return recorded;
    }
}
