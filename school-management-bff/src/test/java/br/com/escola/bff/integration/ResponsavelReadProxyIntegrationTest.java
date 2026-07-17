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
class ResponsavelReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer RESPONSIBLES = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.responsibles-service.base-url", () -> RESPONSIBLES.url("/").toString());
        registry.add("clients.responsibles-service.internal-token", () -> "responsibles-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
        RESPONSIBLES.shutdown();
    }

    @Test
    void deveConsumirResponsiblesServiceNaListagemMinimaDeResponsaveis() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        RESPONSIBLES.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000601",
                          "nomeCompleto":"Maria Souza"
                        }]
                        """));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/responsaveis")
                        .queryParam("nome", "Maria")
                        .queryParam("cpf", "98765432100")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-0")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Maria Souza");

        var contextRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var responsiblesRequest = RESPONSIBLES.takeRequest();
        assertThat(responsiblesRequest.getPath()).isEqualTo("/internal/v1/responsaveis?nome=Maria&cpf=98765432100");
    }

    @Test
    void deveConsumirResponsiblesServiceNoDetalheDeResponsavel() throws InterruptedException {
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        RESPONSIBLES.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "id":"%s",
                          "nomeCompleto":"Responsavel Oficial"
                        }
                        """.formatted(responsavelId)));

        client.get().uri("/api/responsaveis/{id}", responsavelId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(responsavelId.toString());

        var contextRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(contextRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var responsiblesRequest = RESPONSIBLES.takeRequest();
        assertThat(responsiblesRequest.getPath()).isEqualTo("/internal/v1/responsaveis/" + responsavelId);
    }

    @Test
    void deveFazerFallbackParaMonolitoNaListagemQuandoResponsiblesServiceFalhar() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        RESPONSIBLES.enqueue(new MockResponse().setResponseCode(503));
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000601",
                          "nomeCompleto":"Monolito Lista"
                        }]
                        """));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/responsaveis")
                        .queryParam("nome", "Maria")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-fallback-0")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Monolito Lista");

        assertThat(RESPONSIBLES.takeRequest().getPath()).isEqualTo("/internal/v1/responsaveis?nome=Maria");
        assertThat(MONOLITH.takeRequest().getPath()).isEqualTo("/api/responsaveis?nome=Maria");
    }

    @Test
    void deveFazerFallbackParaMonolitoNoDetalheQuandoResponsiblesServiceFalhar() throws InterruptedException {
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        RESPONSIBLES.enqueue(new MockResponse().setResponseCode(503));
        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "id":"%s",
                          "nomeCompleto":"Monolito Detalhe"
                        }
                        """.formatted(responsavelId)));

        client.get().uri("/api/responsaveis/{id}", responsavelId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-responsavel-fallback-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nomeCompleto").isEqualTo("Monolito Detalhe");

        assertThat(RESPONSIBLES.takeRequest().getPath()).isEqualTo("/internal/v1/responsaveis/" + responsavelId);
        assertThat(MONOLITH.takeRequest().getPath()).isEqualTo("/api/responsaveis/" + responsavelId);
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
