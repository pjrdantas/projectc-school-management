package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
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
class AlunoReadProxyIntegrationTest {

    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer PEOPLE = startServer();
    private static final UUID ALUNO_ID = UUID.fromString("00000000-0000-0000-0000-000000000701");
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.people-service.base-url", () -> PEOPLE.url("/").toString());
        registry.add("clients.people-service.internal-token", () -> "people-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        IDENTITY_ACCESS.shutdown();
        PEOPLE.shutdown();
    }

    @Test
    void deveOficializarLeiturasDeAlunoExclusivamentePeloPeopleService() throws InterruptedException {
        enfileirarContexto();
        PEOPLE.enqueue(json("""
                [{"id":"%s","nomeCompleto":"Aluno Listado"}]
                """.formatted(ALUNO_ID)));

        client.get().uri("/api/alunos?nome=Aluno")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-aluno-lista")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(ALUNO_ID.toString());

        assertContextRequest("corr-aluno-lista");
        assertPeopleRequest("/internal/v1/alunos?nome=Aluno", "corr-aluno-lista");

        enfileirarContexto();
        PEOPLE.enqueue(json("""
                {"id":"%s","nomeCompleto":"Aluno Detalhe"}
                """.formatted(ALUNO_ID)));

        client.get().uri("/api/alunos/{alunoId}", ALUNO_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-aluno-detalhe")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(ALUNO_ID.toString());

        assertContextRequest("corr-aluno-detalhe");
        assertPeopleRequest("/internal/v1/alunos/" + ALUNO_ID, "corr-aluno-detalhe");

        enfileirarContexto();
        PEOPLE.enqueue(json("""
                {"aluno":{"id":"%s","nomeCompleto":"Aluno Ficha"},"responsaveis":[]}
                """.formatted(ALUNO_ID)));

        client.get().uri("/api/alunos/{alunoId}/ficha", ALUNO_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-aluno-ficha")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.aluno.id").isEqualTo(ALUNO_ID.toString())
                .jsonPath("$.responsaveis").isArray();

        assertContextRequest("corr-aluno-ficha");
        assertPeopleRequest("/internal/v1/alunos/" + ALUNO_ID + "/ficha", "corr-aluno-ficha");
    }

    @Test
    void deveEncaminharWritesDeAlunoExclusivamentePeloPeopleService() throws InterruptedException {
        String body = "{\"nomeCompleto\":\"Aluno Novo\"}";
        enfileirarContexto();
        PEOPLE.enqueue(json("{\"id\":\"%s\",\"nomeCompleto\":\"Aluno Novo\"}".formatted(ALUNO_ID)));
        client.post().uri("/api/alunos").contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token").header(TrustedHeaders.CORRELATION_ID, "corr-aluno-create")
                .exchange().expectStatus().isCreated();
        assertContextRequest("corr-aluno-create");
        assertPeopleWriteRequest("POST", "/internal/v1/alunos", body);

        enfileirarContexto();
        PEOPLE.enqueue(json("{\"id\":\"%s\",\"nomeCompleto\":\"Aluno Alterado\"}".formatted(ALUNO_ID)));
        client.put().uri("/api/alunos/{id}", ALUNO_ID).contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token").header(TrustedHeaders.CORRELATION_ID, "corr-aluno-update")
                .exchange().expectStatus().isOk();
        assertContextRequest("corr-aluno-update");
        assertPeopleWriteRequest("PUT", "/internal/v1/alunos/" + ALUNO_ID, body);

        enfileirarContexto();
        PEOPLE.enqueue(new MockResponse().setResponseCode(204));
        client.delete().uri("/api/alunos/{id}", ALUNO_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token").header(TrustedHeaders.CORRELATION_ID, "corr-aluno-delete")
                .exchange().expectStatus().isNoContent();
        assertContextRequest("corr-aluno-delete");
        assertPeopleWriteRequest("DELETE", "/internal/v1/alunos/" + ALUNO_ID, null);
    }

    private static void enfileirarContexto() {
        IDENTITY_ACCESS.enqueue(json("""
                {
                  "usuarioId":"%s",
                  "escolaId":"%s",
                  "escolaNome":"Escola padrao"
                }
                """.formatted(USUARIO_ID, ESCOLA_ID)));
    }

    private static void assertContextRequest(String correlationId) throws InterruptedException {
        var request = IDENTITY_ACCESS.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(request.getHeader(TrustedHeaders.CORRELATION_ID)).isEqualTo(correlationId);
    }

    private static void assertPeopleRequest(String path, String correlationId) throws InterruptedException {
        var request = PEOPLE.takeRequest();
        assertThat(request.getPath()).isEqualTo(path);
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(request.getHeader("X-Internal-Token")).isEqualTo("people-internal-token");
        assertThat(request.getHeader(TrustedHeaders.CORRELATION_ID)).isEqualTo(correlationId);
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo(USUARIO_ID.toString());
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo(ESCOLA_ID.toString());
    }

    private static void assertPeopleWriteRequest(String method, String path, String body) throws InterruptedException {
        var request = PEOPLE.takeRequest();
        assertThat(request.getMethod()).isEqualTo(method);
        assertThat(request.getPath()).isEqualTo(path);
        if (body != null) {
            assertThat(request.getBody().readUtf8()).isEqualTo(body);
        }
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
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
