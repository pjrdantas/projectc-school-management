package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
class ProfessorWriteProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY = startServer();
    private static final MockWebServer PROFESSOR = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-internal-token");
        registry.add("clients.academic-professor-service.base-url", () -> PROFESSOR.url("/").toString());
        registry.add("clients.academic-professor-service.internal-token", () -> "professor-internal-token");
        registry.add("features.academic-professor-write-proxy-enabled", () -> true);
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY.shutdown();
        PROFESSOR.shutdown();
    }

    @Test
    void deveCriarProfessorNoServicoAcademicoSemAlterarContratoExterno() throws Exception {
        enqueueContext();
        PROFESSOR.enqueue(json("""
                {"id":"00000000-0000-0000-0000-000000000011","nomeCompleto":"Ana Souza","ativo":true}
                """, 201));

        client.post().uri("/api/professores")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-professor-create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"funcionarioId\":\"00000000-0000-0000-0000-000000000031\",\"registroProfissional\":\"RP-123\",\"formacao\":\"Licenciatura\",\"ativo\":true}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.nomeCompleto").isEqualTo("Ana Souza");

        assertThat(IDENTITY.takeRequest().getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        var request = PROFESSOR.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/professores");
        assertThat(request.getHeader("X-Internal-Token")).isEqualTo("professor-internal-token");
        assertThat(request.getBody().readUtf8()).contains("\"funcionarioId\"").contains("\"RP-123\"");
    }

    @Test
    void deveCriarAlocacaoNoServicoAcademicoSemAlterarContratoExterno() throws Exception {
        String professorId = "00000000-0000-0000-0000-000000000011";
        enqueueContext();
        PROFESSOR.enqueue(json("""
                {"id":"00000000-0000-0000-0000-000000000041","professorId":"00000000-0000-0000-0000-000000000011","turmaDisciplinaId":"00000000-0000-0000-0000-000000000051","ativo":true}
                """, 201));

        client.post().uri("/api/professores/{id}/turmas-disciplinas", professorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-alocacao-create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"turmaDisciplinaId\":\"00000000-0000-0000-0000-000000000051\",\"dataInicio\":\"2026-02-01\",\"ativo\":true}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.professorId").isEqualTo(professorId);

        IDENTITY.takeRequest();
        var request = PROFESSOR.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/v1/professores/" + professorId + "/turmas-disciplinas");
        assertThat(request.getBody().readUtf8()).contains("\"turmaDisciplinaId\"");
    }

    @Test
    void naoDeveFazerFallbackAoMonolitoQuandoCriacaoDeProfessorFalhar() throws Exception {
        enqueueContext();
        PROFESSOR.enqueue(new MockResponse().setResponseCode(503));

        client.post().uri("/api/professores")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-professor-fail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"funcionarioId\":\"00000000-0000-0000-0000-000000000031\",\"ativo\":true}")
                .exchange()
                .expectStatus().isEqualTo(503);

        IDENTITY.takeRequest();
        assertThat(PROFESSOR.takeRequest().getPath()).isEqualTo("/internal/v1/professores");
        assertThat(MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void naoDeveFazerFallbackAoMonolitoQuandoCriacaoDeAlocacaoFalhar() throws Exception {
        String professorId = "00000000-0000-0000-0000-000000000011";
        enqueueContext();
        PROFESSOR.enqueue(new MockResponse().setResponseCode(503));

        client.post().uri("/api/professores/{id}/turmas-disciplinas", professorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-alocacao-fail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"turmaDisciplinaId\":\"00000000-0000-0000-0000-000000000051\",\"ativo\":true}")
                .exchange()
                .expectStatus().isEqualTo(503);

        IDENTITY.takeRequest();
        assertThat(PROFESSOR.takeRequest().getPath()).isEqualTo("/internal/v1/professores/" + professorId + "/turmas-disciplinas");
        assertThat(MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
    }

    private static void enqueueContext() {
        IDENTITY.enqueue(json("""
                {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao"}
                """, 200));
    }

    private static MockResponse json(String body, int status) {
        return new MockResponse().setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }

    private static MockWebServer startServer() {
        try {
            MockWebServer server = new MockWebServer();
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
