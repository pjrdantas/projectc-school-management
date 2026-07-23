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
class ProfessorReadProxyIntegrationTest {

    private static final MockWebServer MONOLITH = startServer();
    private static final MockWebServer IDENTITY_ACCESS = startServer();
    private static final MockWebServer ACADEMIC_PROFESSOR = startServer();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.monolith.base-url", () -> MONOLITH.url("/").toString());
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY_ACCESS.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.academic-professor-service.base-url", () -> ACADEMIC_PROFESSOR.url("/").toString());
        registry.add("clients.academic-professor-service.connect-timeout", () -> "2s");
        registry.add("clients.academic-professor-service.response-timeout", () -> "2s");
        registry.add("clients.academic-professor-service.internal-token", () -> "academic-professor-internal-token");
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        MONOLITH.shutdown();
        IDENTITY_ACCESS.shutdown();
        ACADEMIC_PROFESSOR.shutdown();
    }

    @Test
    void deveConsumirAcademicProfessorServiceNaListagemOficialDeProfessores() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        ACADEMIC_PROFESSOR.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "pessoaId":"00000000-0000-0000-0000-000000000021",
                          "funcionarioId":"00000000-0000-0000-0000-000000000031",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "nomeCompleto":"Ana Souza",
                          "ativo":true
                        }]
                        """));

        client.get().uri("/api/professores")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-prof-1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(TrustedHeaders.CORRELATION_ID, "corr-prof-1")
                .expectBody()
                .jsonPath("$[0].nomeCompleto").isEqualTo("Ana Souza");

        var authRequest = IDENTITY_ACCESS.takeRequest();
        assertThat(authRequest.getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
        assertThat(authRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(authRequest.getHeader(TrustedHeaders.CORRELATION_ID)).isEqualTo("corr-prof-1");

        var professorRequest = ACADEMIC_PROFESSOR.takeRequest();
        assertThat(professorRequest.getPath()).isEqualTo("/internal/v1/professores");
        assertThat(professorRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer opaque-token");
        assertThat(professorRequest.getHeader("X-Internal-Token")).isEqualTo("academic-professor-internal-token");
        assertThat(professorRequest.getHeader("X-Correlation-Id")).isEqualTo("corr-prof-1");
        assertThat(professorRequest.getHeader("X-Usuario-Id")).isEqualTo("00000000-0000-0000-0000-000000000101");
        assertThat(professorRequest.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
    }

    @Test
    void deveConsumirAcademicProfessorServiceNaBuscaOficialDeProfessorPorId() throws InterruptedException {
        String professorId = "00000000-0000-0000-0000-000000000011";

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        ACADEMIC_PROFESSOR.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "pessoaId":"00000000-0000-0000-0000-000000000021",
                          "funcionarioId":"00000000-0000-0000-0000-000000000031",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "nomeCompleto":"Ana Souza",
                          "ativo":true
                        }
                        """));

        client.get().uri("/api/professores/{professorId}", professorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-prof-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.professorId").isEqualTo(professorId)
                .jsonPath("$.nomeCompleto").isEqualTo("Ana Souza");

        IDENTITY_ACCESS.takeRequest();
        var professorRequest = ACADEMIC_PROFESSOR.takeRequest();
        assertThat(professorRequest.getPath()).isEqualTo("/internal/v1/professores/" + professorId);
    }

    @Test
    void deveConsumirAcademicProfessorServiceNaListagemDeAlocacoesPorProfessor() throws InterruptedException {
        String professorId = "00000000-0000-0000-0000-000000000011";

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        ACADEMIC_PROFESSOR.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000041",
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "professorNome":"Ana Souza",
                          "turmaDisciplinaId":"00000000-0000-0000-0000-000000000051",
                          "turmaId":"00000000-0000-0000-0000-000000000061",
                          "turmaNome":"1A",
                          "disciplinaId":"00000000-0000-0000-0000-000000000071",
                          "disciplinaNome":"Matematica",
                          "ativo":true
                        }]
                        """));

        client.get().uri("/api/professores/{professorId}/turmas-disciplinas", professorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-prof-3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].professorId").isEqualTo(professorId)
                .jsonPath("$[0].disciplinaNome").isEqualTo("Matematica");

        IDENTITY_ACCESS.takeRequest();
        var professorRequest = ACADEMIC_PROFESSOR.takeRequest();
        assertThat(professorRequest.getPath()).isEqualTo("/internal/v1/professores/" + professorId + "/turmas-disciplinas");
    }

    @Test
    void deveConsumirAcademicProfessorServiceNaListagemDeProfessoresPorTurma() throws InterruptedException {
        String turmaId = "00000000-0000-0000-0000-000000000061";

        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        ACADEMIC_PROFESSOR.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "id":"00000000-0000-0000-0000-000000000041",
                          "professorId":"00000000-0000-0000-0000-000000000011",
                          "professorNome":"Ana Souza",
                          "turmaDisciplinaId":"00000000-0000-0000-0000-000000000051",
                          "turmaId":"00000000-0000-0000-0000-000000000061",
                          "turmaNome":"1A",
                          "disciplinaId":"00000000-0000-0000-0000-000000000071",
                          "disciplinaNome":"Matematica",
                          "ativo":true
                        }]
                        """));

        client.get().uri("/api/turmas/{turmaId}/professores", turmaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-prof-4")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].turmaId").isEqualTo(turmaId)
                .jsonPath("$[0].professorNome").isEqualTo("Ana Souza");

        IDENTITY_ACCESS.takeRequest();
        var professorRequest = ACADEMIC_PROFESSOR.takeRequest();
        assertThat(professorRequest.getPath()).isEqualTo("/internal/v1/turmas/" + turmaId + "/professores");
    }

    @Test
    void deveConsumirAcademicProfessorServiceNaListagemDeFuncionariosElegiveis() throws InterruptedException {
        IDENTITY_ACCESS.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "usuarioId":"00000000-0000-0000-0000-000000000101",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao"
                        }
                        """));

        ACADEMIC_PROFESSOR.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [{
                          "funcionarioId":"00000000-0000-0000-0000-000000000031",
                          "pessoaId":"00000000-0000-0000-0000-000000000021",
                          "nomeCompleto":"Ana Souza",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "cargo":"Professor",
                          "ativo":true,
                          "elegivelProfessor":true
                        }]
                        """));

        client.get().uri("/api/professores/funcionarios-elegiveis")
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-prof-5")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].funcionarioId").isEqualTo("00000000-0000-0000-0000-000000000031")
                .jsonPath("$[0].elegivelProfessor").isEqualTo(true);

        IDENTITY_ACCESS.takeRequest();
        var professorRequest = ACADEMIC_PROFESSOR.takeRequest();
        assertThat(professorRequest.getPath()).isEqualTo("/internal/v1/professores/funcionarios-elegiveis");
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
