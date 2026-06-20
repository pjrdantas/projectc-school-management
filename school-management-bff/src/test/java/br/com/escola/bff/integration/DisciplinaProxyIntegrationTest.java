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
class DisciplinaProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startMonolith();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopMonolith() throws IOException {
        MONOLITH.shutdown();
    }

    @Test
    void deveExecutarFluxoCompletoDaRotaPiloto() throws InterruptedException {
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000001",
                          "nome":"Matematica",
                          "cargaHoraria":80,
                          "status":"ATIVA",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "createdAt":"2026-06-19T10:00:00"
                        }]
                        """));

        client.get().uri("/api/disciplinas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-e2e")
                .header("X-Escola-Id", "forged")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-e2e")
                .expectBody()
                .jsonPath("$[0].nome").isEqualTo("Matematica");

        var request = MONOLITH.takeRequest();
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(request.getHeader(TrustedHeaders.CORRELATION_ID)).isEqualTo("corr-e2e");
        assertThat(request.getHeader("X-Escola-Id")).isNull();
    }

    @Test
    void deveRejeitarSemBearerAntesDeChamarMonolito() {
        int requestsBefore = MONOLITH.getRequestCount();

        client.get().uri("/api/disciplinas")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED");

        assertThat(MONOLITH.getRequestCount()).isEqualTo(requestsBefore);
    }

    private static MockWebServer startMonolith() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
