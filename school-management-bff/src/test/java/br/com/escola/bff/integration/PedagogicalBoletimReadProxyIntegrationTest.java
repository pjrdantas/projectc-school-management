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
class PedagogicalBoletimReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer PEDAGOGICAL = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
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
    void deveConsumirPedagogicalServiceNaConsultaDeBoletim() throws InterruptedException {
        UUID matriculaId = UUID.randomUUID();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "matriculaId":"%s",
                          "alunoNome":"Aluno BFF",
                          "itens":[{"disciplinaNome":"Matematica"}]
                        }
                        """.formatted(matriculaId)));

        client.get().uri("/api/matriculas/{matriculaId}/boletim", matriculaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-bff-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.matriculaId").isEqualTo(matriculaId.toString())
                .jsonPath("$.alunoNome").isEqualTo("Aluno BFF")
                .jsonPath("$.itens[0].disciplinaNome").isEqualTo("Matematica");

        IDENTITY_ACCESS.takeRequest();
        var pedagogicalRequest = PEDAGOGICAL.takeRequest();
        assertThat(pedagogicalRequest.getPath()).isEqualTo("/internal/v1/matriculas/" + matriculaId + "/boletim");
        assertThat(pedagogicalRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(pedagogicalRequest.getHeader("X-Internal-Token")).isEqualTo("pedagogical-internal-token");
        assertThat(pedagogicalRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-bff-1");
        assertThat(pedagogicalRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveConsumirPedagogicalServiceNaListagemDeFechamentosDeBoletim() throws InterruptedException {
        UUID matriculaId = UUID.randomUUID();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [
                          {
                            "matriculaId":"%s",
                            "periodoReferencia":"1BIM",
                            "persistido":true
                          }
                        ]
                        """.formatted(matriculaId)));

        client.get().uri("/api/matriculas/{matriculaId}/boletim/fechamentos", matriculaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-bff-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].matriculaId").isEqualTo(matriculaId.toString())
                .jsonPath("$[0].periodoReferencia").isEqualTo("1BIM")
                .jsonPath("$[0].persistido").isEqualTo(true);

        IDENTITY_ACCESS.takeRequest();
        var pedagogicalRequest = PEDAGOGICAL.takeRequest();
        assertThat(pedagogicalRequest.getPath()).isEqualTo("/internal/v1/matriculas/" + matriculaId + "/boletim/fechamentos");
        assertThat(pedagogicalRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(pedagogicalRequest.getHeader("X-Internal-Token")).isEqualTo("pedagogical-internal-token");
        assertThat(pedagogicalRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-bff-2");
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
