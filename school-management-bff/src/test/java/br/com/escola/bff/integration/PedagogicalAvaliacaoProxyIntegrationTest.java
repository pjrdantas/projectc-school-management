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
class PedagogicalAvaliacaoProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer PEDAGOGICAL = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
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
    void deveConsumirPedagogicalServiceNaCriacaoDeAvaliacao() throws InterruptedException {
        UUID alocacaoId = UUID.randomUUID();
        String requestBody = """
                {"professorTurmaDisciplinaId":"%s","titulo":"Prova fase 130","descricao":"Avaliacao integrada","dataAplicacao":"2039-04-15","valorMaximo":10.00,"peso":1.00,"tipoAvaliacao":"PROVA"}
                """.formatted(alocacaoId);

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"%s","professorTurmaDisciplinaId":"%s","turmaNome":"Turma Avaliacao","tipoAvaliacao":"PROVA"}
                        """.formatted(UUID.randomUUID(), alocacaoId)));

        client.post().uri("/api/avaliacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-avaliacao-write-1")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.professorTurmaDisciplinaId").isEqualTo(alocacaoId.toString());

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes");
        assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveConsumirPedagogicalServiceNaListagemDeAvaliacoes() throws InterruptedException {
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID avaliacaoId = UUID.randomUUID();

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{"id":"%s","professorTurmaDisciplinaId":"%s","turmaId":"%s","turmaNome":"Turma Avaliacao","tipoAvaliacao":"PROVA"}]
                        """.formatted(avaliacaoId, alocacaoId, turmaId)));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/avaliacoes")
                        .queryParam("professorTurmaDisciplinaId", alocacaoId)
                        .queryParam("turmaId", turmaId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-avaliacao-read-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(avaliacaoId.toString())
                .jsonPath("$[0].turmaId").isEqualTo(turmaId.toString());

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes?professorTurmaDisciplinaId=" + alocacaoId + "&turmaId=" + turmaId);
    }

    @Test
    void deveConsumirPedagogicalServiceNaBuscaDeAvaliacaoPorId() throws InterruptedException {
        UUID avaliacaoId = UUID.randomUUID();

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"%s","turmaNome":"Turma Avaliacao","tipoAvaliacao":"PROVA"}
                        """.formatted(avaliacaoId)));

        client.get().uri("/api/avaliacoes/{id}", avaliacaoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-avaliacao-read-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(avaliacaoId.toString())
                .jsonPath("$.turmaNome").isEqualTo("Turma Avaliacao");

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes/" + avaliacaoId);
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-avaliacao-read-2");
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
