package br.com.escola.institutionaltenantservice.interfaces.rest;

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
class TenantSessaoInternaControllerIntegrationTest {

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
        registry.add("institutional-tenant.internal-api.token", () -> "institutional-token");
        registry.add("institutional-tenant.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveListarEscolasDisponiveisNoContratoInterno() throws Exception {
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
                            "escolaNome":"Escola Reserva",
                            "ativa":false
                          }
                        ]
                        """.formatted(escolaAtivaId, outraEscolaId)));

        mockMvc.perform(get("/internal/v1/tenant/escolas")
                        .header("X-Internal-Token", "institutional-token")
                        .header("X-Correlation-Id", "corr-tenant-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer tenant-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").value(escolaAtivaId.toString()))
                .andExpect(jsonPath("$[0].ativa").value(true))
                .andExpect(jsonPath("$[1].escolaNome").value("Escola Reserva"));

        RecordedRequest recorded = aguardarRequisicao("/internal/auth/escolas");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer tenant-user-token");
    }

    @Test
    void deveRetornarTenantAtivoDerivadoDaListaDeEscolasDaSessao() throws Exception {
        UUID escolaAtivaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "escolaId":"%s",
                            "escolaNome":"Escola Principal",
                            "ativa":true
                          }
                        ]
                        """.formatted(escolaAtivaId)));

        mockMvc.perform(get("/internal/v1/tenant/ativa")
                        .header("X-Internal-Token", "institutional-token")
                        .header("X-Correlation-Id", "corr-tenant-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer tenant-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value(escolaAtivaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola Principal"));

        RecordedRequest recorded = aguardarRequisicao("/internal/auth/escolas");
        assertThat(recorded.getMethod()).isEqualTo("GET");
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/tenant/escolas")
                        .header("X-Correlation-Id", "corr-tenant-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer tenant-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    private RecordedRequest aguardarRequisicao(String path) throws InterruptedException {
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getPath()).isEqualTo(path);
        return recorded;
    }
}

