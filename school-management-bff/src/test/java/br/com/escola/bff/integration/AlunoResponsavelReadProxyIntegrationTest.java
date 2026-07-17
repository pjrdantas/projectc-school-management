package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

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
class AlunoResponsavelReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer RESPONSIBLES = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.responsibles-service.base-url", () -> RESPONSIBLES.url("/").toString());
        registry.add("clients.responsibles-service.internal-token", () -> "responsibles-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
        RESPONSIBLES.shutdown();
    }

    @Test
    void deveConsumirResponsiblesServiceNaLeituraOficialDeResponsaveisPorAluno() throws InterruptedException {
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000301");

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        RESPONSIBLES.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000401",
                          "nomeCompleto":"Responsavel Teste",
                          "parentesco":"MAE"
                        }]
                        """));

        client.get().uri("/api/alunos/{alunoId}/responsaveis", alunoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-resp-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Responsavel Teste");

        var contextRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var responsiblesRequest = RESPONSIBLES.takeRequest();
        assertThat(responsiblesRequest.getPath()).isEqualTo("/internal/v1/alunos/" + alunoId + "/responsaveis");
        assertThat(responsiblesRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(responsiblesRequest.getHeader("X-Internal-Token")).isEqualTo("responsibles-internal-token");
        assertThat(responsiblesRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-resp-1");
        assertThat(responsiblesRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(responsiblesRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoResponsiblesServiceFalharNaLeituraPorAluno() throws InterruptedException {
        UUID alunoId = UUID.fromString("00000000-0000-0000-0000-000000000301");

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        RESPONSIBLES.enqueue(new MockResponse().setResponseCode(503));
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000401",
                          "nomeCompleto":"Monolito Vinculo",
                          "parentesco":"MAE"
                        }]
                        """));

        client.get().uri("/api/alunos/{alunoId}/responsaveis", alunoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-resp-fallback-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Monolito Vinculo");

        assertThat(RESPONSIBLES.takeRequest().getPath()).isEqualTo("/internal/v1/alunos/" + alunoId + "/responsaveis");
        assertThat(MONOLITH.takeRequest().getPath()).isEqualTo("/api/alunos/" + alunoId + "/responsaveis");
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
