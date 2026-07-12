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
class ConsultaCadastralReadProxyIntegrationTest {

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
    void deveConsumirPeopleServiceNaConsultaCadastralOficial() throws InterruptedException {
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
                          "content":[{
                            "alunoId":"00000000-0000-0000-0000-000000000301",
                            "nomeAluno":"Aluno Teste",
                            "responsaveis":[]
                          }],
                          "totalElements":1,
                          "page":0,
                          "size":20
                        }
                        """));

        client.get().uri("/api/consulta-cadastral?nomeAluno=Aluno&page=0&size=20")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-consulta-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].nomeAluno").isEqualTo("Aluno Teste");

        MONOLITH.takeRequest();
        var peopleRequest = PEOPLE.takeRequest();
        assertThat(peopleRequest.getPath()).startsWith("/internal/v1/pessoas/consulta-cadastral?");
        assertThat(peopleRequest.getPath()).contains("nomeAluno=Aluno");
        assertThat(peopleRequest.getPath()).contains("page=0");
        assertThat(peopleRequest.getPath()).contains("size=20");
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
