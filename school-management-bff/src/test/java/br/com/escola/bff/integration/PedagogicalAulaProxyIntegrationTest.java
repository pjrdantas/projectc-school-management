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
class PedagogicalAulaProxyIntegrationTest {

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
    void deveConsumirPedagogicalServiceNaCriacaoDeAula() throws InterruptedException {
        UUID alocacaoId = UUID.randomUUID();
        String requestBody = """
                {"professorTurmaDisciplinaId":"%s","dataAula":"2038-03-10","realizada":true}
                """.formatted(alocacaoId);

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

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/aulas");
        assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
    }

    @Test
    void deveConsumirPedagogicalServiceNaListagemDeAulas() throws InterruptedException {
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{"id":"%s","professorTurmaDisciplinaId":"%s","turmaId":"%s","turmaNome":"Turma Aula","realizada":true}]
                        """.formatted(aulaId, alocacaoId, turmaId)));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/aulas")
                        .queryParam("professorTurmaDisciplinaId", alocacaoId)
                        .queryParam("turmaId", turmaId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-aula-read-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(aulaId.toString())
                .jsonPath("$[0].turmaId").isEqualTo(turmaId.toString());

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/aulas?professorTurmaDisciplinaId=" + alocacaoId + "&turmaId=" + turmaId);
    }

    @Test
    void deveConsumirPedagogicalServiceNaBuscaDeAulaPorId() throws InterruptedException {
        UUID aulaId = UUID.randomUUID();

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"id":"%s","turmaNome":"Turma Aula","realizada":true}
                        """.formatted(aulaId)));

        client.get().uri("/api/aulas/{id}", aulaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-aula-read-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(aulaId.toString())
                .jsonPath("$.turmaNome").isEqualTo("Turma Aula");

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/aulas/" + aulaId);
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-aula-read-2");
    }

    @Test
    void deveConsumirPedagogicalServiceNoRegistroDeFrequenciaProfessor() throws InterruptedException {
        UUID aulaId = UUID.randomUUID();
        String requestBody = """
                {"presente":true,"justificativa":"Presente"}
                """;

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
    }

    @Test
    void deveConsumirPedagogicalServiceNaListagemDeFrequenciaProfessor() throws InterruptedException {
        UUID aulaId = UUID.randomUUID();

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{"id":"%s","aulaId":"%s","professorNome":"Professor Aula","presente":true}]
                        """.formatted(UUID.randomUUID(), aulaId)));

        client.get().uri("/api/aulas/{id}/frequencia-professor", aulaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-aula-freq-prof-read-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].aulaId").isEqualTo(aulaId.toString());

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/aulas/" + aulaId + "/frequencia-professor");
    }

    @Test
    void deveConsumirPedagogicalServiceNoRegistroDeFrequenciaAluno() throws InterruptedException {
        UUID aulaId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();
        String requestBody = """
                {"matriculaId":"%s","situacao":"PRESENTE","justificativa":"Participou"}
                """.formatted(matriculaId);

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
    }

    @Test
    void deveConsumirPedagogicalServiceNaListagemDeFrequenciasAlunos() throws InterruptedException {
        UUID aulaId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{"id":"%s","aulaId":"%s","matriculaId":"%s","alunoNome":"Aluno Aula","situacao":"PRESENTE"}]
                        """.formatted(UUID.randomUUID(), aulaId, matriculaId)));

        client.get().uri("/api/aulas/{id}/frequencias-alunos", aulaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-aula-freq-aluno-read-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].aulaId").isEqualTo(aulaId.toString())
                .jsonPath("$[0].matriculaId").isEqualTo(matriculaId.toString());

        IDENTITY_ACCESS.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/aulas/" + aulaId + "/frequencias-alunos");
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
