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
class PeriodoLetivoWriteMonolithIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("features.catalog-write-cutover.enabled", () -> false);
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServer() throws IOException {
        MONOLITH.shutdown();
    }

    @Test
    void deveVoltarParaMonolitoQuandoCutoverDeEscritaEstiverDesabilitado() throws Exception {
        MONOLITH.enqueue(json("""
                {
                  "id":"00000000-0000-0000-0000-000000000091",
                  "nome":"2026",
                  "ano":2026,
                  "dataInicio":"2026-01-10",
                  "dataFim":"2026-12-20",
                  "ativo":true,
                  "escolaId":"00000000-0000-0000-0000-000000000047",
                  "escolaNome":"Escola monolito",
                  "createdAt":"2026-06-23T09:05:00"
                }
                """, 201));

        client.post().uri("/api/periodos-letivos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-write-monolith")
                .header("Idempotency-Key", "idem-fixed")
                .header("X-Escola-Id", "forged-school")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "nome":"2026",
                          "ano":2026,
                          "dataInicio":"2026-01-10",
                          "dataFim":"2026-12-20"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.escolaNome").isEqualTo("Escola monolito");

        var monolithRequest = MONOLITH.takeRequest();
        assertThat(monolithRequest.getPath()).isEqualTo("/api/periodos-letivos");
        assertThat(monolithRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(monolithRequest.getHeader("X-Escola-Id")).isNull();
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

    private static MockResponse json(String body, int status) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }
}
