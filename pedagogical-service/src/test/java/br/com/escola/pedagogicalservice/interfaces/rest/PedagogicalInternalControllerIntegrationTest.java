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

    private RecordedRequest aguardarRequisicao(String method, String path) throws InterruptedException {
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getMethod()).isEqualTo(method);
        assertThat(recorded.getPath()).isEqualTo(path);
        return recorded;
    }
}
