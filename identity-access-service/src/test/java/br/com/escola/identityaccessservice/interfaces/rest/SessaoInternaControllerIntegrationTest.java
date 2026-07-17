package br.com.escola.identityaccessservice.interfaces.rest;

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
class SessaoInternaControllerIntegrationTest {

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
        registry.add("identity-access.internal-api.token", () -> "identity-token");
        registry.add("identity-access.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveListarEscolasDaSessaoNoContratoInterno() throws Exception {
        UUID escolaAtivaId = UUID.randomUUID();
        UUID outraEscolaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "escolaId":"%s",
                            "escolaNome":"Escola Ativa",
                            "ativa":true
                          },
                          {
                            "escolaId":"%s",
                            "escolaNome":"Escola Opcional",
                            "ativa":false
                          }
                        ]
                        """.formatted(escolaAtivaId, outraEscolaId)));

        mockMvc.perform(get("/internal/v1/auth/escolas")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer identity-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").value(escolaAtivaId.toString()))
                .andExpect(jsonPath("$[0].ativa").value(true))
                .andExpect(jsonPath("$[1].escolaNome").value("Escola Opcional"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/auth/escolas");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer identity-user-token");
    }

    @Test
    void deveConsultarContextoAtualNoContratoInterno() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "usuarioId":"%s",
                          "escolaId":"%s",
                          "escolaNome":"Escola Contexto",
                          "username":"usuario.contexto"
                        }
                        """.formatted(usuarioId, escolaId)));

        mockMvc.perform(get("/internal/v1/auth/contexto-atual")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-ctx")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer identity-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola Contexto"))
                .andExpect(jsonPath("$.username").value("usuario.contexto"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/api/auth/contexto-atual");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer identity-user-token");
    }

    @Test
    void deveSelecionarEscolaAtivaNoContratoInterno() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "usuarioId":"%s",
                          "escolaId":"%s",
                          "escolaNome":"Escola Selecionada",
                          "username":"usuario.identity"
                        }
                        """.formatted(usuarioId, escolaId)));

        mockMvc.perform(post("/internal/v1/auth/escola-ativa")
                        .contentType("application/json")
                        .content("""
                                {
                                  "escolaId":"%s"
                                }
                                """.formatted(escolaId))
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer identity-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andExpect(jsonPath("$.username").value("usuario.identity"));

        RecordedRequest recorded = aguardarRequisicao("POST", "/internal/auth/escola-ativa");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer identity-user-token");
        assertThat(recorded.getBody().readUtf8()).contains(escolaId.toString());
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/auth/escolas")
                        .header("X-Correlation-Id", "corr-identity-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer identity-user-token"))
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

