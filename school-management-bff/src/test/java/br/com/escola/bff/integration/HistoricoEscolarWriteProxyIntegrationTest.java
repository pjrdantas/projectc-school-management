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
class HistoricoEscolarWriteProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer PEDAGOGICAL = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.pedagogical-service.base-url", () -> PEDAGOGICAL.url("/").toString());
        registry.add("clients.pedagogical-service.internal-token", () -> "pedagogical-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        PEDAGOGICAL.shutdown();
    }

    @Test
    void deveConsumirPedagogicalServiceNaCriacaoDeHistorico() throws InterruptedException {
        UUID alunoId = UUID.randomUUID();
        String requestBody = """
                {"nomeAluno":"Aluno","alunoId":"%s","componentesCurriculares":[]}
                """.formatted(alunoId);

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"%s","alunoId":"%s","nomeAluno":"Aluno","componentesCurriculares":[]}
                        """.formatted(UUID.randomUUID(), alunoId)));

        client.post().uri("/api/historicos-escolares")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-history-write-1")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.alunoId").isEqualTo(alunoId.toString());

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/historicos-escolares");
        assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveConsumirPedagogicalServiceNaAtualizacaoDeHistorico() throws InterruptedException {
        UUID historicoId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        String requestBody = """
                {"nomeAluno":"Aluno Atualizado","alunoId":"%s","componentesCurriculares":[]}
                """.formatted(alunoId);

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"%s","alunoId":"%s","nomeAluno":"Aluno Atualizado","componentesCurriculares":[]}
                        """.formatted(historicoId, alunoId)));

        client.put().uri("/api/historicos-escolares/{id}", historicoId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-history-write-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(historicoId.toString())
                .jsonPath("$.nomeAluno").isEqualTo("Aluno Atualizado");

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/historicos-escolares/" + historicoId);
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-history-write-2");
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

