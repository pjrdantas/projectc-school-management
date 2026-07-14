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
class PedagogicalDiarioClasseReadProxyIntegrationTest {

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
    void deveConsumirPedagogicalServiceNaLeituraDeDiarioClasse() throws InterruptedException {
        UUID professorId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                        """));

        PEDAGOGICAL.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"cabecalho":{"idProfessor":"%s","idTurma":"%s","idDisciplina":"%s","anoLetivo":2058,"mes":6},"alunos":[{"nome":"Aluno Diario"}],"bloqueado":false}
                        """.formatted(professorId, turmaId, disciplinaId)));

        client.get().uri(uriBuilder -> uriBuilder.path("/api/diarios-classe")
                        .queryParam("idProfessor", professorId)
                        .queryParam("idTurma", turmaId)
                        .queryParam("idDisciplina", disciplinaId)
                        .queryParam("anoLetivo", 2058)
                        .queryParam("mes", 6)
                        .queryParam("dataReferencia", "2058-06-26")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-pedagogical-diario-read-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.cabecalho.idProfessor").isEqualTo(professorId.toString())
                .jsonPath("$.alunos[0].nome").isEqualTo("Aluno Diario")
                .jsonPath("$.bloqueado").isEqualTo(false);

        var authRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(authRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(authRequest.getHeader("X-Internal-Token")).isEqualTo("identity-access-internal-token");
        var request = PEDAGOGICAL.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/diarios-classe?idProfessor=" + professorId
                + "&idTurma=" + turmaId
                + "&idDisciplina=" + disciplinaId
                + "&anoLetivo=2058&mes=6&dataReferencia=2058-06-26");
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo("corr-pedagogical-diario-read-1");
        assertThat(MONOLITH.getRequestCount()).isZero();
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
