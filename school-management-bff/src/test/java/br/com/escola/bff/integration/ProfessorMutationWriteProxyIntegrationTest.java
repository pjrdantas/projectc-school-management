package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProfessorMutationWriteProxyIntegrationTest {

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
    void deveEncaminharTodasAsMutacoesAoServicoDeProfessores() throws Exception {
        for (Mutation mutation : mutations()) {
            enqueueContext();
            PROFESSOR.enqueue(response(mutation.responseBody(), mutation.status()));

            exchange(mutation).expectStatus().isEqualTo(mutation.status());

            assertThat(IDENTITY.takeRequest().getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
            var request = PROFESSOR.takeRequest();
            assertThat(request.getMethod()).isEqualTo(mutation.method().name());
            assertThat(request.getPath()).isEqualTo(mutation.internalPath());
            assertThat(request.getHeader("X-Internal-Token")).isEqualTo("professor-internal-token");
        }
    }

    @Test
    void naoDeveFazerFallbackAoMonolitoQuandoCadaMutacaoFalhar() throws Exception {
        for (Mutation mutation : mutations()) {
            enqueueContext();
            PROFESSOR.enqueue(new MockResponse().setResponseCode(503));

            exchange(mutation).expectStatus().isEqualTo(503);

            IDENTITY.takeRequest();
            var request = PROFESSOR.takeRequest();
            assertThat(request.getMethod()).isEqualTo(mutation.method().name());
            assertThat(request.getPath()).isEqualTo(mutation.internalPath());
            assertThat(MONOLITH.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
        }
    }

    private WebTestClient.ResponseSpec exchange(Mutation mutation) {
        if (mutation.requestBody() == null) {
            return client.method(mutation.method()).uri(mutation.publicPath())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                    .header(TrustedHeaders.CORRELATION_ID, "corr-professor-mutation")
                    .exchange();
        }
        return client.method(mutation.method()).uri(mutation.publicPath())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-professor-mutation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mutation.requestBody())
                .exchange();
    }

    private static List<Mutation> mutations() {
        String professorId = "00000000-0000-0000-0000-000000000011";
        String alocacaoId = "00000000-0000-0000-0000-000000000041";
        String allocationPath = "/api/professores/" + professorId + "/turmas-disciplinas/" + alocacaoId;
        String internalAllocationPath = "/internal/v1/professores/" + professorId + "/turmas-disciplinas/" + alocacaoId;
        return List.of(
                new Mutation(HttpMethod.PUT, "/api/professores/" + professorId,
                        "/internal/v1/professores/" + professorId,
                        "{\"registroProfissional\":\"RP-999\",\"formacao\":\"Mestrado\",\"ativo\":false}",
                        "{\"id\":\"" + professorId + "\",\"ativo\":false}", 200),
                new Mutation(HttpMethod.PUT, allocationPath, internalAllocationPath,
                        "{\"turmaDisciplinaId\":\"00000000-0000-0000-0000-000000000051\",\"ativo\":true}",
                        "{\"id\":\"" + alocacaoId + "\",\"ativo\":true}", 200),
                new Mutation(HttpMethod.DELETE, allocationPath, internalAllocationPath, null, null, 204));
    }

    private static void enqueueContext() {
        IDENTITY.enqueue(response(
                "{\"usuarioId\":\"00000000-0000-0000-0000-000000000101\",\"escolaId\":\"00000000-0000-0000-0000-000000000047\",\"escolaNome\":\"Escola padrao\"}", 200));
    }

    private static MockResponse response(String body, int status) {
        MockResponse response = new MockResponse().setResponseCode(status);
        if (body != null) {
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(body);
        }
        return response;
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

    private record Mutation(
            HttpMethod method,
            String publicPath,
            String internalPath,
            String requestBody,
            String responseBody,
            int status) {
    }
}
