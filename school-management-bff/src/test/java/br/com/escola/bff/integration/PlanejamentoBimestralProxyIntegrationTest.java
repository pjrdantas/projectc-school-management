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
class PlanejamentoBimestralProxyIntegrationTest {

    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer PLANNING_AI = startServer();
    private static final MockWebServer ACADEMIC_PROFESSOR = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.planning-ai-service.base-url", () -> PLANNING_AI.url("/").toString());
        registry.add("clients.planning-ai-service.internal-token", () -> "planning-ai-internal-token");
        registry.add("clients.academic-professor-service.base-url", () -> ACADEMIC_PROFESSOR.url("/").toString());
        registry.add("clients.academic-professor-service.internal-token", () -> "academic-professor-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        IDENTITY_ACCESS.shutdown();
        PLANNING_AI.shutdown();
        ACADEMIC_PROFESSOR.shutdown();
    }

    @Test
    void deveEncaminharOperacoesRaizParaPlanningAiSemAdapterLegado() throws Exception {
        String planejamentoId = "00000000-0000-0000-0000-000000000711";
        String vinculoId = "00000000-0000-0000-0000-000000000712";
        String periodoId = "00000000-0000-0000-0000-000000000713";
        String body = """
                {"professorTurmaDisciplinaId":"%s","periodoAvaliativoId":"%s","titulo":"Plano","temaPrincipal":"Fracoes","descricaoInicial":"Base"}
                """.formatted(vinculoId, periodoId);

        enfileirarContexto(7);
        PLANNING_AI.enqueue(json(201, "{\"id\":\"" + planejamentoId + "\",\"titulo\":\"Plano\"}"));
        PLANNING_AI.enqueue(json(200, "[{\"id\":\"" + planejamentoId + "\"}]"));
        PLANNING_AI.enqueue(json(200, "{\"id\":\"" + planejamentoId + "\"}"));
        PLANNING_AI.enqueue(json(200, "{\"id\":\"" + planejamentoId + "\",\"titulo\":\"Plano atualizado\"}"));
        PLANNING_AI.enqueue(json(200, "{\"id\":\"" + planejamentoId + "\",\"status\":\"EM_ANALISE\"}"));
        PLANNING_AI.enqueue(json(201, "{\"id\":\"00000000-0000-0000-0000-000000000714\"}"));
        PLANNING_AI.enqueue(json(201, "{\"id\":\"00000000-0000-0000-0000-000000000715\"}"));

        requisicaoPost("/api/planejamentos-bimestrais", body).expectStatus().isCreated();
        requisicaoGet("/api/planejamentos-bimestrais?professorTurmaDisciplinaId=" + vinculoId
                + "&periodoAvaliativoId=" + periodoId).expectStatus().isOk();
        requisicaoGet("/api/planejamentos-bimestrais/" + planejamentoId).expectStatus().isOk();
        requisicaoPut("/api/planejamentos-bimestrais/" + planejamentoId, body).expectStatus().isOk();
        requisicaoPatch("/api/planejamentos-bimestrais/" + planejamentoId + "/status", "{\"status\":\"EM_ANALISE\"}")
                .expectStatus().isOk();
        requisicaoPost("/api/planejamentos-bimestrais/" + planejamentoId + "/aulas-previstas",
                "{\"numeroAula\":1,\"temaAula\":\"Fracoes\"}").expectStatus().isCreated();
        requisicaoPost("/api/planejamentos-bimestrais/" + planejamentoId + "/avaliacoes-previstas",
                "{\"titulo\":\"Avaliacao\",\"tipoAvaliacao\":\"PROVA\"}").expectStatus().isCreated();

        assertThat(PLANNING_AI.takeRequest().getPath()).isEqualTo("/internal/v1/planejamentos-bimestrais");
        assertThat(PLANNING_AI.takeRequest().getPath()).isEqualTo(
                "/internal/v1/planejamentos-bimestrais?professorTurmaDisciplinaId=" + vinculoId
                        + "&periodoAvaliativoId=" + periodoId);
        assertThat(PLANNING_AI.takeRequest().getPath()).isEqualTo("/internal/v1/planejamentos-bimestrais/" + planejamentoId);
        assertThat(PLANNING_AI.takeRequest().getPath()).isEqualTo("/internal/v1/planejamentos-bimestrais/" + planejamentoId);
        assertThat(PLANNING_AI.takeRequest().getPath()).isEqualTo(
                "/internal/v1/planejamentos-bimestrais/" + planejamentoId + "/status");
        assertThat(PLANNING_AI.takeRequest().getPath()).isEqualTo(
                "/internal/v1/planejamentos-bimestrais/" + planejamentoId + "/aulas-previstas");
        assertThat(PLANNING_AI.takeRequest().getPath()).isEqualTo(
                "/internal/v1/planejamentos-bimestrais/" + planejamentoId + "/avaliacoes-previstas");
        assertThat(IDENTITY_ACCESS.getRequestCount()).isGreaterThanOrEqualTo(7);
    }

    @Test
    void deveRetornarIndisponibilidadeSemFallbackQuandoPlanningAiFalhar() throws Exception {
        enfileirarContexto(1);
        PLANNING_AI.enqueue(new MockResponse().setResponseCode(503));

        requisicaoGet("/api/planejamentos-bimestrais")
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

        assertThat(PLANNING_AI.takeRequest().getPath()).isEqualTo("/internal/v1/planejamentos-bimestrais");
    }

    @Test
    void deveCompatibilizarFiltroLegadoDeProfessorSemConsultarMonolito() throws Exception {
        String professorId = "00000000-0000-0000-0000-000000000716";
        String vinculoId = "00000000-0000-0000-0000-000000000712";
        enfileirarContexto(1);
        ACADEMIC_PROFESSOR.enqueue(json(200, """
                [{"professorId":"%s","turmaDisciplinaId":"%s","turmaId":"00000000-0000-0000-0000-000000000717","disciplinaId":"00000000-0000-0000-0000-000000000718"}]
                """.formatted(professorId, vinculoId)));
        PLANNING_AI.enqueue(json(200, "[{\"id\":\"00000000-0000-0000-0000-000000000711\"}]"));

        requisicaoGet("/api/planejamentos-bimestrais?professorId=" + professorId)
                .expectStatus().isOk()
                .expectBody().jsonPath("$[0].id").isEqualTo("00000000-0000-0000-0000-000000000711");

        assertThat(ACADEMIC_PROFESSOR.takeRequest().getPath())
                .isEqualTo("/internal/v1/professores/" + professorId + "/turmas-disciplinas");
        assertThat(PLANNING_AI.takeRequest().getPath())
                .isEqualTo("/internal/v1/planejamentos-bimestrais?professorTurmaDisciplinaId=" + vinculoId);
    }

    private WebTestClient.ResponseSpec requisicaoGet(String uri) {
        return client.get().uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-bimestral")
                .exchange();
    }

    private WebTestClient.ResponseSpec requisicaoPost(String uri, String body) {
        return client.post().uri(uri).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-bimestral")
                .bodyValue(body).exchange();
    }

    private WebTestClient.ResponseSpec requisicaoPut(String uri, String body) {
        return client.put().uri(uri).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-bimestral")
                .bodyValue(body).exchange();
    }

    private WebTestClient.ResponseSpec requisicaoPatch(String uri, String body) {
        return client.patch().uri(uri).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-planejamento-bimestral")
                .bodyValue(body).exchange();
    }

    private static void enfileirarContexto(int quantidade) {
        for (int indice = 0; indice < quantidade; indice++) {
            IDENTITY_ACCESS.enqueue(json(200, """
                    {"usuarioId":"00000000-0000-0000-0000-000000000101","escolaId":"00000000-0000-0000-0000-000000000047"}
                    """));
        }
    }

    private static MockResponse json(int status, String body) {
        return new MockResponse().setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
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
