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
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PedagogicalAvaliacaoProxyIntegrationTest {

    private static final MockWebServer IDENTITY_ACCESS = startIdentityAccessServer();
    private static final MockWebServer MONOLITH = startMonolithServer();
    private static final MockWebServer PEDAGOGICAL = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.pedagogical-service.base-url", () -> PEDAGOGICAL.url("/").toString());
        registry.add("clients.pedagogical-service.internal-token", () -> "pedagogical-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        IDENTITY_ACCESS.shutdown();
        MONOLITH.shutdown();
        PEDAGOGICAL.shutdown();
    }

    @Test
    void deveConsumirPedagogicalServiceNaCriacaoDeAvaliacao() throws InterruptedException {
        UUID alocacaoId = UUID.randomUUID();
        String requestBody = """
                {"professorTurmaDisciplinaId":"%s","titulo":"Prova fase 130","descricao":"Avaliacao integrada","dataAplicacao":"2039-04-15","valorMaximo":10.00,"peso":1.00,"tipoAvaliacao":"PROVA"}
                """.formatted(alocacaoId);

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

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes?professorTurmaDisciplinaId=" + alocacaoId + "&turmaId=" + turmaId);
    }

    @Test
    void deveConsumirPedagogicalServiceNaBuscaDeAvaliacaoPorId() throws InterruptedException {
        UUID avaliacaoId = UUID.randomUUID();

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

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes/" + avaliacaoId);
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-avaliacao-read-2");
    }

    @Test
    void deveConsumirPedagogicalServiceNoLancamentoDeNota() throws InterruptedException {
        UUID avaliacaoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();
        String requestBody = """
                {"matriculaId":"%s","nota":8.50,"observacao":"Boa participacao"}
                """.formatted(matriculaId);

        PEDAGOGICAL.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"%s","avaliacaoId":"%s","matriculaId":"%s","alunoNome":"Aluno Nota","nota":8.50}
                        """.formatted(UUID.randomUUID(), avaliacaoId, matriculaId)));

        client.post().uri("/api/avaliacoes/{id}/notas", avaliacaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-nota-write-1")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.avaliacaoId").isEqualTo(avaliacaoId.toString())
                .jsonPath("$.matriculaId").isEqualTo(matriculaId.toString());

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes/" + avaliacaoId + "/notas");
        assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveConsumirPedagogicalServiceNaListagemDeNotasPorAvaliacao() throws InterruptedException {
        UUID avaliacaoId = UUID.randomUUID();

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{"id":"%s","avaliacaoId":"%s","alunoNome":"Aluno Nota","nota":8.50}]
                        """.formatted(UUID.randomUUID(), avaliacaoId)));

        client.get().uri("/api/avaliacoes/{id}/notas", avaliacaoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-nota-read-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].avaliacaoId").isEqualTo(avaliacaoId.toString())
                .jsonPath("$[0].alunoNome").isEqualTo("Aluno Nota");

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes/" + avaliacaoId + "/notas");
    }

    @Test
    void deveConsumirPedagogicalServiceNaListagemDeNotasPorMatricula() throws InterruptedException {
        UUID matriculaId = UUID.randomUUID();

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{"id":"%s","matriculaId":"%s","alunoNome":"Aluno Matricula Nota","nota":9.00}]
                        """.formatted(UUID.randomUUID(), matriculaId)));

        client.get().uri("/api/matriculas/{matriculaId}/notas", matriculaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-nota-read-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].matriculaId").isEqualTo(matriculaId.toString())
                .jsonPath("$[0].alunoNome").isEqualTo("Aluno Matricula Nota");

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/matriculas/" + matriculaId + "/notas");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-nota-read-2");
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

    private static MockWebServer startMonolithServer() {
        MockWebServer server = startServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if ("/api/auth/contexto-atual".equals(request.getPath())) {
                    return authContextResponse();
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        return server;
    }

    private static MockWebServer startIdentityAccessServer() {
        MockWebServer server = startServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if ("/internal/v1/auth/contexto-atual".equals(request.getPath())) {
                    return authContextResponse();
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        return server;
    }

    private static MockResponse authContextResponse() {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """);
    }
}
