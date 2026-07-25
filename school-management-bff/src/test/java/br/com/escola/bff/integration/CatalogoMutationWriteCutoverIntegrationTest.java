package br.com.escola.bff.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
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
class CatalogoMutationWriteCutoverIntegrationTest {

    private static final MockWebServer IDENTITY = startServer();
    private static final MockWebServer CATALOG = startServer();
    private static final Path REPORT_PATH = createReportFile();

    @Autowired
    private WebTestClient client;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("clients.identity-access-service.base-url", () -> IDENTITY.url("/").toString());
        registry.add("clients.identity-access-service.internal-token", () -> "identity-access-internal-token");
        registry.add("clients.catalog-service.base-url", () -> CATALOG.url("/").toString());
        registry.add("clients.catalog-service.internal-token", () -> "catalog-internal-token");
        registry.add("features.catalog-write-proxy-enabled", () -> true);
        registry.add("features.catalog-read-cutover.report-path", () -> REPORT_PATH.toString());
        registry.add("management.health.redis.enabled", () -> false);
    }

    @AfterAll
    static void stopServers() throws IOException {
        IDENTITY.shutdown();
        CATALOG.shutdown();
        Files.deleteIfExists(REPORT_PATH);
    }

    @Test
    void deveEncaminharTodasAsMutacoesParaOCatalogoOficial() throws Exception {
        for (Mutation mutation : mutations()) {
            enqueueContext();
            if (mutation.resolverPath() != null) {
                CATALOG.enqueue(json(mutation.resolverBody()));
            }
            CATALOG.enqueue(mutation.successResponse());

            exchange(mutation).expectStatus().isEqualTo(mutation.successStatus());

            assertThat(IDENTITY.takeRequest().getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
            if (mutation.resolverPath() != null) {
                assertThat(CATALOG.takeRequest().getPath()).isEqualTo(mutation.resolverPath());
            }
            var catalogRequest = CATALOG.takeRequest();
            assertThat(catalogRequest.getMethod()).isEqualTo(mutation.method().name());
            assertThat(catalogRequest.getPath()).isEqualTo(mutation.internalPath());
            assertThat(catalogRequest.getHeader("X-Internal-Token")).isEqualTo("catalog-internal-token");
            assertThat(catalogRequest.getHeader("Idempotency-Key")).isNotBlank();
        }
    }

    @Test
    void naoDeveFazerFallbackParaOMonolitoQuandoCadaMutacaoFalharNoCatalogo() throws Exception {
        for (Mutation mutation : mutations()) {
            enqueueContext();
            if (mutation.resolverPath() != null) {
                CATALOG.enqueue(json(mutation.resolverBody()));
            }
            CATALOG.enqueue(new MockResponse().setResponseCode(503));

            exchange(mutation).expectStatus().isEqualTo(503)
                    .expectBody().jsonPath("$.code").isEqualTo("CATALOG_UNAVAILABLE");

            assertThat(IDENTITY.takeRequest().getPath()).isEqualTo("/internal/v1/auth/contexto-atual");
            if (mutation.resolverPath() != null) {
                assertThat(CATALOG.takeRequest().getPath()).isEqualTo(mutation.resolverPath());
            }
            var catalogRequest = CATALOG.takeRequest();
            assertThat(catalogRequest.getMethod()).isEqualTo(mutation.method().name());
            assertThat(catalogRequest.getPath()).isEqualTo(mutation.internalPath());
            assertThat(IDENTITY.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
        }
    }

    private WebTestClient.ResponseSpec exchange(Mutation mutation) {
        if (mutation.body() == null) {
            return client.method(mutation.method()).uri(mutation.publicPath())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                    .header(TrustedHeaders.CORRELATION_ID, "corr-catalog-mutation")
                    .header("Idempotency-Key", "mutation-key")
                    .exchange();
        }
        return client.method(mutation.method()).uri(mutation.publicPath())
                .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                .header(TrustedHeaders.CORRELATION_ID, "corr-catalog-mutation")
                .header("Idempotency-Key", "mutation-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mutation.body())
                .exchange();
    }

    private static List<Mutation> mutations() {
        String periodId = "00000000-0000-0000-0000-000000000011";
        String subjectId = "00000000-0000-0000-0000-000000000012";
        String seriesId = "00000000-0000-0000-0000-000000000013";
        String classId = "00000000-0000-0000-0000-000000000014";
        String linkId = "00000000-0000-0000-0000-000000000015";
        String turnoId = "00000000-0000-0000-0000-000000000016";
        return List.of(
                mutation(HttpMethod.POST, "/api/turnos", "/internal/v1/turnos", turnoBody(), turnoResponse(), 201),
                mutation(HttpMethod.PUT, "/api/turnos/" + turnoId, "/internal/v1/turnos/" + turnoId, turnoBody(), turnoResponse(), 200),
                mutation(HttpMethod.PUT, "/api/periodos-letivos/" + periodId, "/internal/v1/periodos-letivos/" + periodId, periodBody(), periodResponse(), 200),
                mutation(HttpMethod.DELETE, "/api/periodos-letivos/" + periodId, "/internal/v1/periodos-letivos/" + periodId, null, null, 204),
                mutation(HttpMethod.PUT, "/api/disciplinas/" + subjectId, "/internal/v1/disciplinas/" + subjectId, subjectBody(), subjectResponse(), 200),
                mutation(HttpMethod.DELETE, "/api/disciplinas/" + subjectId, "/internal/v1/disciplinas/" + subjectId, null, null, 204),
                mutationWithResolver(HttpMethod.PUT, "/api/series/" + seriesId, "/internal/v1/series/" + seriesId, seriesBody(), seriesResponse(), "/internal/v1/catalogos/niveis-ensino", niveisResponse(), 200),
                mutation(HttpMethod.DELETE, "/api/series/" + seriesId, "/internal/v1/series/" + seriesId, null, null, 204),
                mutationWithResolver(HttpMethod.PUT, "/api/turmas/" + classId, "/internal/v1/turmas/" + classId, classBody(), classResponse(), "/internal/v1/turnos", turnosResponse(), 200),
                mutation(HttpMethod.DELETE, "/api/turmas/" + classId, "/internal/v1/turmas/" + classId, null, null, 204),
                mutation(HttpMethod.PUT, "/api/turmas/" + classId + "/disciplinas/" + linkId, "/internal/v1/turmas/" + classId + "/disciplinas/" + linkId, linkBody(), linkResponse(), 200),
                mutation(HttpMethod.DELETE, "/api/turmas/" + classId + "/disciplinas/" + linkId, "/internal/v1/turmas/" + classId + "/disciplinas/" + linkId, null, null, 204));
    }

    private static Mutation mutation(HttpMethod method, String publicPath, String internalPath, String body, String response, int status) {
        return new Mutation(method, publicPath, internalPath, body, response, null, null, status);
    }

    private static Mutation mutationWithResolver(HttpMethod method, String publicPath, String internalPath, String body, String response, String resolverPath, String resolverBody, int status) {
        return new Mutation(method, publicPath, internalPath, body, response, resolverPath, resolverBody, status);
    }

    private void enqueueContext() {
        IDENTITY.enqueue(json("""
                {"usuarioId":"00000000-0000-0000-0000-000000000201","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao","username":"admin"}
                """));
    }

    private static MockResponse json(String body) {
        return new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(body);
    }

    private static MockResponse response(String body, int status) {
        return new MockResponse().setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(body);
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

    private static Path createReportFile() {
        try {
            Path path = Files.createTempFile("catalog-mutation-write-cutover-report", ".json");
            Files.writeString(path, "{\"applied\":true,\"reconciled\":true,\"sourceIssues\":[],\"targetIssues\":[],\"tables\":[{\"missingIds\":[],\"unexpectedIds\":[],\"divergentIds\":[]}]}");
            return path;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static String periodBody() { return "{\"nome\":\"2026\",\"ano\":2026,\"dataInicio\":\"2026-02-01\",\"dataFim\":\"2026-12-20\"}"; }
    private static String periodResponse() { return "{\"id\":\"00000000-0000-0000-0000-000000000011\",\"nome\":\"2026\",\"ano\":2026,\"dataInicio\":\"2026-02-01\",\"dataFim\":\"2026-12-20\",\"ativo\":true,\"escolaId\":\"00000000-0000-0000-0000-000000000047\",\"createdAt\":\"2026-01-01T10:00:00\"}"; }
    private static String subjectBody() { return "{\"nome\":\"Matematica\",\"cargaHoraria\":80,\"status\":\"ATIVA\"}"; }
    private static String subjectResponse() { return "{\"id\":\"00000000-0000-0000-0000-000000000012\",\"nome\":\"Matematica\",\"cargaHoraria\":80,\"ativo\":true,\"escolaId\":\"00000000-0000-0000-0000-000000000047\",\"createdAt\":\"2026-01-01T10:00:00\"}"; }
    private static String seriesBody() { return "{\"nome\":\"1 ano\",\"ordem\":1,\"nivelEnsino\":\"FUNDAMENTAL\"}"; }
    private static String seriesResponse() { return "{\"id\":\"00000000-0000-0000-0000-000000000013\",\"nome\":\"1 ano\",\"ordem\":1,\"nivelEnsinoCodigo\":\"FUNDAMENTAL\",\"escolaId\":\"00000000-0000-0000-0000-000000000047\",\"createdAt\":\"2026-01-01T10:00:00\"}"; }
    private static String classBody() { return "{\"codigo\":\"1A\",\"nome\":\"1 Ano A\",\"capacidade\":30,\"periodoLetivoId\":\"00000000-0000-0000-0000-000000000011\",\"serieId\":\"00000000-0000-0000-0000-000000000013\",\"turno\":\"MATUTINO\",\"status\":\"ATIVA\"}"; }
    private static String classResponse() { return "{\"id\":\"00000000-0000-0000-0000-000000000014\",\"codigo\":\"1A\",\"nome\":\"1 Ano A\",\"capacidade\":30,\"periodoLetivoId\":\"00000000-0000-0000-0000-000000000011\",\"serieId\":\"00000000-0000-0000-0000-000000000013\",\"serieNome\":\"1 ano\",\"turnoCodigo\":\"MATUTINO\",\"ativo\":true,\"escolaId\":\"00000000-0000-0000-0000-000000000047\",\"createdAt\":\"2026-01-01T10:00:00\"}"; }
    private static String linkBody() { return "{\"disciplinaId\":\"00000000-0000-0000-0000-000000000012\",\"cargaHoraria\":80}"; }
    private static String linkResponse() { return "{\"id\":\"00000000-0000-0000-0000-000000000015\",\"turmaId\":\"00000000-0000-0000-0000-000000000014\",\"disciplinaId\":\"00000000-0000-0000-0000-000000000012\",\"disciplinaNome\":\"Matematica\",\"cargaHoraria\":80,\"createdAt\":\"2026-01-01T10:00:00\"}"; }
    private static String niveisResponse() { return "[{\"id\":\"00000000-0000-0000-0000-000000000021\",\"codigo\":\"FUNDAMENTAL\",\"descricao\":\"Fundamental\"}]"; }
    private static String turnosResponse() { return "[{\"id\":\"00000000-0000-0000-0000-000000000022\",\"codigo\":\"MATUTINO\",\"descricao\":\"Matutino\"}]"; }
    private static String turnoBody() { return "{\"codigo\":\"NOTURNO\",\"descricao\":\"Noturno\"}"; }
    private static String turnoResponse() { return "{\"id\":\"00000000-0000-0000-0000-000000000016\",\"codigo\":\"NOTURNO\",\"descricao\":\"Noturno\"}"; }

    private record Mutation(HttpMethod method, String publicPath, String internalPath, String body,
                            String successBody, String resolverPath, String resolverBody, int successStatus) {
        MockResponse successResponse() { return successStatus == 204 ? new MockResponse().setResponseCode(204) : response(successBody, successStatus); }
    }
}
