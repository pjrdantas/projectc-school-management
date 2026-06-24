package br.com.escola.professorservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "management.endpoint.health.show-details=always",
                "professor.shadow.internal-api.token=shadow-token"
        })
class ProfessorShadowOperationalSmokeIntegrationTest {

    private static MockWebServer mockWebServer;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("professor.shadow.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveComprovarSinaisDeSucessoNotFoundEIndisponibilidadeNoHealthDoShadow() {
        UUID professorId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(201)
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Smoke Write",
                          "escolaId": "00000000-0000-0000-0000-000000000047",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-WRITE",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-23T10:15:30",
                          "updatedAt": "2026-06-23T10:15:30"
                        }
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Professor Smoke",
                            "escolaId": "00000000-0000-0000-0000-000000000047",
                            "escolaNome": "Escola Padrao",
                            "registroProfissional": "RP-SMOKE",
                            "formacao": "Licenciatura",
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30",
                            "updatedAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        RestClient shadowClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-Internal-Token", "shadow-token")
                .defaultHeader("X-Correlation-Id", "corr-shadow-smoke")
                .defaultHeader("X-Usuario-Id", UUID.randomUUID().toString())
                .defaultHeader("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer shadow-user-token")
                .build();

        String criarResponse = shadowClient.post()
                .uri("/internal/v1/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "funcionarioId": "%s",
                          "registroProfissional": "RP-WRITE",
                          "formacao": "Licenciatura",
                          "ativo": true
                        }
                        """.formatted(funcionarioId))
                .retrieve()
                .body(String.class);
        assertThat(criarResponse).contains("Professor Smoke Write");

        String listarResponse = shadowClient.get()
                .uri("/internal/v1/professores")
                .retrieve()
                .body(String.class);
        assertThat(listarResponse).contains("Professor Smoke");

        try {
            shadowClient.get()
                    .uri("/internal/v1/professores/{id}", professorId)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException exception) {
            assertThat(exception.getStatusCode().value()).isEqualTo(404);
        }

        try {
            shadowClient.get()
                    .uri("/internal/v1/professores/funcionarios-elegiveis")
                    .retrieve()
                    .body(String.class);
        } catch (HttpServerErrorException exception) {
            assertThat(exception.getStatusCode().value()).isEqualTo(503);
        }

        RestClient actuatorClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<?, ?> health = actuatorClient.get()
                .uri("/actuator/health/professorShadowMonolith")
                .retrieve()
                .body(Map.class);

        assertThat(health).isNotNull();
        assertThat(health.get("status")).isEqualTo("UP");

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.get("details");
        assertThat(details)
                .containsEntry("dependency", "monolith")
                .containsEntry("requestsTotal", 4.0d)
                .containsEntry("failuresTotal", 1.0d);

        @SuppressWarnings("unchecked")
        Map<String, Object> shadowReadRoutes = (Map<String, Object>) details.get("shadowReadRoutes");
        @SuppressWarnings("unchecked")
        Map<String, Object> criar = (Map<String, Object>) shadowReadRoutes.get("criar");
        @SuppressWarnings("unchecked")
        Map<String, Object> listar = (Map<String, Object>) shadowReadRoutes.get("listar");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorIdRoute = (Map<String, Object>) shadowReadRoutes.get("buscarPorId");
        @SuppressWarnings("unchecked")
        Map<String, Object> elegiveis = (Map<String, Object>) shadowReadRoutes.get("listarFuncionariosElegiveis");

        assertThat(criar)
                .containsEntry("shadowRoute", "POST /internal/v1/professores")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(listar)
                .containsEntry("shadowRoute", "GET /internal/v1/professores")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(buscarPorIdRoute)
                .containsEntry("shadowRoute", "GET /internal/v1/professores/{id}")
                .containsEntry("monolithNotFoundTotal", 1.0d);
        assertThat(elegiveis)
                .containsEntry("shadowRoute", "GET /internal/v1/professores/funcionarios-elegiveis")
                .containsEntry("monolithErrorTotal", 1.0d)
                .containsEntry("failuresTotal", 1.0d);
    }
}
