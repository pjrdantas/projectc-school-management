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
class PedagogicalHistoricoEscolarReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer PEDAGOGICAL = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.pedagogical-service.base-url", () -> PEDAGOGICAL.url("/").toString());
        registry.add("clients.pedagogical-service.internal-token", () -> "pedagogical-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        PEDAGOGICAL.shutdown();
    }

    @Test
    void deveConsumirPedagogicalServiceNoCarregamentoNovoDeHistorico() throws InterruptedException {
        UUID alunoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"contexto":{"idAluno":"%s","idMatricula":"%s","modo":"CADASTRO"}}
                        """.formatted(alunoId, matriculaId)));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/historicos-escolares/novo")
                        .queryParam("idAluno", alunoId)
                        .queryParam("idMatricula", matriculaId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-history-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.contexto.idAluno").isEqualTo(alunoId.toString())
                .jsonPath("$.contexto.idMatricula").isEqualTo(matriculaId.toString())
                .jsonPath("$.contexto.modo").isEqualTo("CADASTRO");

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/historicos-escolares/novo?idAluno=" + alunoId + "&idMatricula=" + matriculaId + "&modo=CADASTRO");
        assertThat(request.getHeader("X-Internal-Token")).isEqualTo("pedagogical-internal-token");
    }

    @Test
    void deveConsumirPedagogicalServiceNoCarregamentoDeEdicaoDeHistorico() throws InterruptedException {
        UUID historicoId = UUID.randomUUID();

        MONOLITH.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"contexto":{"idHistoricoEscolar":"%s","modo":"EDICAO"}}
                        """.formatted(historicoId)));

        client.get().uri("/api/historicos-escolares/{id}/carregamento", historicoId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-history-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.contexto.idHistoricoEscolar").isEqualTo(historicoId.toString())
                .jsonPath("$.contexto.modo").isEqualTo("EDICAO");

        MONOLITH.takeRequest();
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/historicos-escolares/" + historicoId + "/carregamento");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-history-2");
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
