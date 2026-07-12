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
class ProfessorReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer PEOPLE = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.people-service.base-url", () -> PEOPLE.url("/").toString());
        registry.add("clients.people-service.internal-token", () -> "people-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        PEOPLE.shutdown();
    }

    @Test
    void deveConsumirPeopleServiceNaListagemOficialDeProfessores() throws InterruptedException {
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        PEOPLE.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "pessoaId":"00000000-0000-0000-0000-000000000021",
                          "funcionarioId":"00000000-0000-0000-0000-000000000031",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "nomeCompleto":"Ana Souza",
                          "ativo":true
                        }]
                        """));

        client.get().uri("/api/professores")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-prof-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-prof-1")
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Ana Souza");

        var authRequest = MONOLITH.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/api/auth/contexto-atual");
        assertThat(authRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(authRequest.getHeader(TrustedHeaders.CORRELATION_ID)).isEqualTo("corr-prof-1");

        var peopleRequest = PEOPLE.takeRequest();
        assertThat(peopleRequest.getPath()).isEqualTo("/internal/v1/professores");
        assertThat(peopleRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(peopleRequest.getHeader("X-Internal-Token")).isEqualTo("people-internal-token");
        assertThat(peopleRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-prof-1");
        assertThat(peopleRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(peopleRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveConsumirPeopleServiceNaBuscaOficialDeProfessorPorId() throws InterruptedException {
        String professorId = "00000000-0000-0000-0000-000000000011";

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        PEOPLE.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "pessoaId":"00000000-0000-0000-0000-000000000021",
                          "funcionarioId":"00000000-0000-0000-0000-000000000031",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "nomeCompleto":"Ana Souza",
                          "ativo":true
                        }
                        """));

        client.get().uri("/api/professores/{professorId}", professorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-prof-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.professorId").isEqualTo(professorId)
                .jsonPath("$.nomeCompleto").isEqualTo("Ana Souza");

        MONOLITH.takeRequest();
        var peopleRequest = PEOPLE.takeRequest();
        assertThat(peopleRequest.getPath()).isEqualTo("/internal/v1/professores/" + professorId);
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
