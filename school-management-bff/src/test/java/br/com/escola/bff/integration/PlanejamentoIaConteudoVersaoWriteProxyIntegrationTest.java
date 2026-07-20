package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PlanejamentoIaConteudoVersaoWriteProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer PLANNING_AI = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.planning-ai-service.base-url", () -> PLANNING_AI.url("/").toString());
        registry.add("clients.planning-ai-service.internal-token", () -> "planning-ai-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        PLANNING_AI.shutdown();
    }

    @Test
    void deveConsumirPlanejamentoIaServiceNaCriacaoOficialDeVersaoConteudoIa() throws InterruptedException {
        String conteudoId = "00000000-0000-0000-0000-000000001011";

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        PLANNING_AI.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "id":"00000000-0000-0000-0000-000000001111",
                          "conteudoGeradoId":"00000000-0000-0000-0000-000000001011",
                          "numeroVersao":2,
                          "conteudo":"Conteudo revisado",
                          "motivoAlteracao":"Ajuste do professor"
                        }
                        """));

        String requestBody = """
                {"conteudo":"Conteudo revisado","motivoAlteracao":"Ajuste do professor"}
                """;

        client.post().uri("/api/ia/conteudos/{conteudoId}/versoes", conteudoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-versao-write-2")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.conteudoGeradoId").isEqualTo(conteudoId)
                .jsonPath("$.numeroVersao").isEqualTo(2);

        MONOLITH.takeRequest();
        var planningRequest = PLANNING_AI.takeRequest();
        assertThat(planningRequest.getPath())
                .isEqualTo("/internal/v1/ia/conteudos/" + conteudoId + "/versoes");
        assertThat(planningRequest.getMethod()).isEqualTo("POST");
        assertThat(planningRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(planningRequest.getHeader("X-Internal-Token")).isEqualTo("planning-ai-internal-token");
        assertThat(planningRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-planejamento-versao-write-2");
        assertThat(planningRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(planningRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(planningRequest.getBody().readUtf8()).isEqualTo(requestBody);
    }

    private static MockWebServer startServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}

