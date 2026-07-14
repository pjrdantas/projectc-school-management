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
class PedagogicalAulaWriteProxyIntegrationTest {

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
    void deveConsumirMonolitoEpedagogicalServiceNaCriacaoDeAula() throws InterruptedException {
        UUID alocacaoId = UUID.randomUUID();
        String requestBody = """
                {"professorTurmaDisciplinaId":"%s","dataAula":"2038-03-10","realizada":true}
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
                        {"id":"%s","professorTurmaDisciplinaId":"%s","turmaNome":"Turma Aula","realizada":true}
                        """.formatted(UUID.randomUUID(), alocacaoId)));

        client.post().uri("/api/aulas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-aula-write-1")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.professorTurmaDisciplinaId").isEqualTo(alocacaoId.toString());

        var authRequest = MONOLITH.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/api/auth/contexto-atual");
        assertThat(authRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");

        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/aulas");
        assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
        assertThat(IDENTITY_ACCESS.getRequestCount()).isZero();
    }

    @Test
    void deveConsumirMonolitoEpedagogicalServiceNoRegistroDeFrequenciaProfessor() throws InterruptedException {
        UUID aulaId = UUID.randomUUID();
        String requestBody = """
                {"presente":true,"justificativa":"Presente"}
                """;

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"%s","aulaId":"%s","professorNome":"Professor Aula","presente":true}
                        """.formatted(UUID.randomUUID(), aulaId)));

        client.post().uri("/api/aulas/{id}/frequencia-professor", aulaId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-aula-freq-prof-write-1")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.aulaId").isEqualTo(aulaId.toString());

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/aulas/" + aulaId + "/frequencia-professor");
        assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
        assertThat(IDENTITY_ACCESS.getRequestCount()).isZero();
    }

    @Test
    void deveConsumirMonolitoEpedagogicalServiceNoRegistroDeFrequenciaAluno() throws InterruptedException {
        UUID aulaId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();
        String requestBody = """
                {"matriculaId":"%s","situacao":"PRESENTE","justificativa":"Participou"}
                """.formatted(matriculaId);

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"%s","aulaId":"%s","matriculaId":"%s","alunoNome":"Aluno Aula","situacao":"PRESENTE"}
                        """.formatted(UUID.randomUUID(), aulaId, matriculaId)));

        client.post().uri("/api/aulas/{id}/frequencias-alunos", aulaId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-aula-freq-aluno-write-1")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.aulaId").isEqualTo(aulaId.toString())
                .jsonPath("$.matriculaId").isEqualTo(matriculaId.toString());

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/aulas/" + aulaId + "/frequencias-alunos");
        assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
        assertThat(IDENTITY_ACCESS.getRequestCount()).isZero();
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
