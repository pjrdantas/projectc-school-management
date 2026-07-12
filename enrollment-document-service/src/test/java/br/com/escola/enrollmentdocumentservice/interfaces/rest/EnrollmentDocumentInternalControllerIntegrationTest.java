package br.com.escola.enrollmentdocumentservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class EnrollmentDocumentInternalControllerIntegrationTest {

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
        registry.add("enrollment-document.internal-api.token", () -> "shadow-token");
        registry.add("enrollment-document.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveCriarTransferenciaNoContratoInterno() throws Exception {
        UUID transferenciaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID escolaOrigemId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody("""
                        {
                          "id": "%s",
                          "alunoId": "%s",
                          "escolaOrigem": {
                            "id": "%s",
                            "nomeEscola": "Escola Origem Interna",
                            "codigoInep": "123",
                            "cnpj": null,
                            "cep": null,
                            "logradouro": null,
                            "numero": null,
                            "complemento": null,
                            "bairro": null,
                            "cidade": null,
                            "uf": null,
                            "createdAt": "2026-07-12T10:00:00"
                          },
                          "serieOrigem": "5A",
                          "anoLetivoOrigem": "2026",
                          "dataTransferencia": "2026-07-01",
                          "motivoTransferencia": "Mudanca",
                          "situacaoOrigem": "ATIVO",
                          "documentosEntregues": "SIM",
                          "observacao": "obs",
                          "tipoTransferencia": "ENTRADA",
                          "statusTransferencia": "EM_ANDAMENTO",
                          "usuarioOperacao": "tester",
                          "dataHoraOperacao": "2026-07-12T10:00:00",
                          "createdAt": "2026-07-12T10:00:00"
                        }
                        """.formatted(transferenciaId, alunoId, escolaOrigemId)));

        mockMvc.perform(post("/internal/v1/transferencias")
                        .contentType("application/json")
                        .content("""
                                {
                                  "alunoId": "%s",
                                  "escolaOrigemId": "%s",
                                  "serieOrigem": "5A",
                                  "anoLetivoOrigem": "2026",
                                  "dataTransferencia": "2026-07-01",
                                  "motivoTransferencia": "Mudanca",
                                  "situacaoOrigem": "ATIVO",
                                  "documentosEntregues": "SIM",
                                  "tipoTransferencia": "ENTRADA",
                                  "statusTransferencia": "EM_ANDAMENTO",
                                  "usuarioOperacao": "tester",
                                  "observacao": "obs"
                                }
                                """.formatted(alunoId, escolaOrigemId))
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-enrollment-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer enrollment-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transferenciaId.toString()))
                .andExpect(jsonPath("$.escolaOrigem.nomeEscola").value("Escola Origem Interna"));

        RecordedRequest recorded = aguardarRequisicao("POST", "/internal/transferencias");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer enrollment-user-token");
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(recorded.getBody().readUtf8()).contains(alunoId.toString());
    }

    @Test
    void deveListarEscolasOrigemNoContratoInterno() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "nomeEscola": "Escola Origem A",
                            "codigoInep": "123",
                            "cnpj": null,
                            "cep": null,
                            "logradouro": null,
                            "numero": null,
                            "complemento": null,
                            "bairro": null,
                            "cidade": null,
                            "uf": null,
                            "createdAt": "2026-07-12T10:00:00"
                          }
                        ]
                        """.formatted(UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/escolas-origem")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-enrollment-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer enrollment-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeEscola").value("Escola Origem A"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/escolas-origem");
        assertThat(recorded.getPath()).isEqualTo("/internal/escolas-origem");
    }

    @Test
    void deveListarDocumentosPorAlunoNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "alunoId":"%s",
                            "tipoDocumento":"HISTORICO_ESCOLAR",
                            "nomeArquivo":"historico.pdf",
                            "urlArquivo":"s3://bucket/historico.pdf",
                            "numeroDocumento":"historico.pdf",
                            "caminhoArquivo":"s3://bucket/historico.pdf",
                            "dataUpload":"2026-07-12T10:00:00",
                            "observacao":"Documento escolar"
                          }
                        ]
                        """.formatted(documentoId, alunoId)));

        mockMvc.perform(get("/internal/v1/documentos-alunos/alunos/{alunoId}", alunoId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-enrollment-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer enrollment-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(documentoId.toString()))
                .andExpect(jsonPath("$[0].alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$[0].tipoDocumento").value("HISTORICO_ESCOLAR"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/documentos-alunos/alunos/" + alunoId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer enrollment-user-token");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-enrollment-4");
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/escolas-origem")
                        .header("X-Correlation-Id", "corr-enrollment-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer enrollment-user-token"))
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
