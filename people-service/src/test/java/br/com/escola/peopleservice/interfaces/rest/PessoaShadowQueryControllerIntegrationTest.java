package br.com.escola.peopleservice.interfaces.rest;

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
class PessoaShadowQueryControllerIntegrationTest {

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
        registry.add("people.shadow.internal-api.token", () -> "shadow-token");
        registry.add("people.shadow.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveConsultarPessoaPorIdNoRuntimeShadow() throws Exception {
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "%s",
                          "nomeCompleto": "Pessoa Shadow",
                          "escolaId": "%s",
                          "escolaNome": "Escola Padrao",
                          "ativo": true
                        }
                        """.formatted(pessoaId, escolaId)));

        mockMvc.perform(get("/internal/v1/pessoas/{id}", pessoaId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-people-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pessoaId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Pessoa Shadow"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/pessoas/" + pessoaId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer shadow-user-token");
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-people-1");
    }

    @Test
    void deveExporRotasInternasCompativeisEConsultaCadastral() throws Exception {
        UUID tipoPessoaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "codigo": "ALUNO",
                            "descricao": "Aluno"
                          }
                        ]
                        """.formatted(tipoPessoaId)));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "content": [
                            {
                              "idAluno": "%s",
                              "nomeCompleto": "Aluno Shadow",
                              "cpf": "12345678901",
                              "email": "aluno.shadow@example.com",
                              "telefone": "11999999999",
                              "dataNascimento": "2014-03-10",
                              "createdAt": "2026-07-02T08:00:00",
                              "responsaveis": [
                                {
                                  "id": "%s",
                                  "nomeCompleto": "Responsavel Shadow",
                                  "cpf": "98765432100",
                                  "email": "responsavel.shadow@example.com",
                                  "telefone": "11888888888",
                                  "createdAt": "2026-07-02T08:30:00"
                                }
                              ]
                            }
                          ],
                          "totalElements": 1,
                          "page": 0,
                          "size": 10
                        }
                        """.formatted(alunoId, responsavelId)));

        mockMvc.perform(get("/internal/pessoas/catalogos/tipos-pessoa")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-people-2a")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tipoPessoaId.toString()))
                .andExpect(jsonPath("$[0].codigo").value("ALUNO"));

        mockMvc.perform(get("/internal/v1/pessoas/consulta-cadastral")
                        .queryParam("nomeAluno", "Aluno")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-people-2b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idAluno").value(alunoId.toString()))
                .andExpect(jsonPath("$.content[0].responsaveis[0].id").value(responsavelId.toString()));

        RecordedRequest catalogoRequest = aguardarRequisicao("GET", "/internal/pessoas/catalogos/tipos-pessoa");
        assertThat(catalogoRequest.getPath()).isEqualTo("/internal/pessoas/catalogos/tipos-pessoa");

        RecordedRequest consultaRequest = aguardarRequisicao("GET", "/internal/pessoas/consulta-cadastral?nomeAluno=Aluno&page=0&size=10");
        assertThat(consultaRequest.getPath()).isEqualTo("/internal/pessoas/consulta-cadastral?nomeAluno=Aluno&page=0&size=10");
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/pessoas/catalogos/tipos-pessoa")
                        .header("X-Correlation-Id", "corr-people-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveMapearPessoaNaoEncontrada() throws Exception {
        UUID pessoaId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        mockMvc.perform(get("/internal/v1/pessoas/{id}", pessoaId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-people-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    private RecordedRequest aguardarRequisicao(String method, String path) throws InterruptedException {
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
            if (request == null) {
                continue;
            }
            if (method.equals(request.getMethod()) && path.equals(request.getPath())) {
                return request;
            }
        }
        throw new AssertionError("Requisicao esperada nao encontrada: " + method + " " + path);
    }
}
