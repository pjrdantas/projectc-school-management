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
class AuthSessionMonolithIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("features.identity-tenant-cutover.enabled", () -> false);
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServer() throws IOException {
        MONOLITH.shutdown();
    }

    @Test
    void deveVoltarDiretamenteParaMonolitoQuandoCutoverDeEscolasDaSessaoEstiverDesabilitado() throws Exception {
        MONOLITH.enqueue(json("""
                [
                  {
                    "escolaId":"00000000-0000-0000-0000-000000000047",
                    "escolaNome":"Escola monolito",
                    "ativa":true
                  }
                ]
                """));

        client.get().uri("/api/auth/escolas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-auth-monolith")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].escolaNome").isEqualTo("Escola monolito");

        var monolithRequest = MONOLITH.takeRequest();
        assertThat(monolithRequest.getPath()).isEqualTo("/internal/auth/escolas");
        assertThat(monolithRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
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

    private static MockResponse json(String body) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }
}
