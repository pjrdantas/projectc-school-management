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
class AvaliacaoReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
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
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
        PEDAGOGICAL.shutdown();
    }

    @Test
    void deveConsumirIdentityAccessEpedagogicalServiceNaListagemDeAvaliacoes() throws InterruptedException {
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID avaliacaoId = UUID.randomUUID();
        int monolithRequestCount = MONOLITH.getRequestCount();

        IDENTITY_ACCESS.enqueue(new MockResponse()
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

        var authRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes?professorTurmaDisciplinaId=" + alocacaoId + "&turmaId=" + turmaId);
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithRequestCount);
    }

    @Test
    void deveConsumirIdentityAccessEpedagogicalServiceNaBuscaDeAvaliacaoPorId() throws InterruptedException {
        UUID avaliacaoId = UUID.randomUUID();
        int monolithRequestCount = MONOLITH.getRequestCount();

        IDENTITY_ACCESS.enqueue(new MockResponse()
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

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/avaliacoes/" + avaliacaoId);
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-avaliacao-read-2");
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithRequestCount);
    }

    @Test
    void deveConsumirIdentityAccessEpedagogicalServiceNaListagemDeNotasPorAvaliacao() throws InterruptedException {
        UUID avaliacaoId = UUID.randomUUID();
        int monolithRequestCount = MONOLITH.getRequestCount();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

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
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithRequestCount);
    }

    @Test
    void deveConsumirIdentityAccessEpedagogicalServiceNaListagemDeNotasPorMatricula() throws InterruptedException {
        UUID matriculaId = UUID.randomUUID();
        int monolithRequestCount = MONOLITH.getRequestCount();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

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
        assertThat(MONOLITH.getRequestCount()).isEqualTo(monolithRequestCount);
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoPedagogicalEstiverIndisponivelNaListagemDeAvaliacoes() throws InterruptedException {
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID.randomUUID();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/avaliacoes")
                        .queryParam("professorTurmaDisciplinaId", alocacaoId)
                        .queryParam("turmaId", turmaId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-avaliacao-read-fallback-1")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        IDENTITY_ACCESS.takeRequest();
        PEDAGOGICAL.takeRequest();
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoPedagogicalEstiverIndisponivelNaBuscaDeAvaliacaoPorId() throws InterruptedException {
        UUID avaliacaoId = UUID.randomUUID();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri("/api/avaliacoes/{id}", avaliacaoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-avaliacao-read-fallback-1b")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        IDENTITY_ACCESS.takeRequest();
        PEDAGOGICAL.takeRequest();
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoPedagogicalEstiverIndisponivelNasNotasPorAvaliacao() throws InterruptedException {
        UUID avaliacaoId = UUID.randomUUID();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri("/api/avaliacoes/{id}/notas", avaliacaoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-avaliacao-read-fallback-2")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        IDENTITY_ACCESS.takeRequest();
        PEDAGOGICAL.takeRequest();
        assertThat(MONOLITH.getRequestCount()).isZero();
    }

    @Test
    void deveRetornarIndisponibilidadeQuandoPedagogicalEstiverIndisponivelNasNotasPorMatricula() throws InterruptedException {
        UUID matriculaId = UUID.randomUUID();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse().setResponseCode(503));

        client.get().uri("/api/matriculas/{matriculaId}/notas", matriculaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-avaliacao-read-fallback-3")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        IDENTITY_ACCESS.takeRequest();
        PEDAGOGICAL.takeRequest();
        assertThat(MONOLITH.getRequestCount()).isZero();
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

