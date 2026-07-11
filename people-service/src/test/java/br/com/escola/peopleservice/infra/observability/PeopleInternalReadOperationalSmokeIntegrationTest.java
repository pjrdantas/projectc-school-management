package br.com.escola.peopleservice.infra.observability;

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
                "people.shadow.internal-api.token=shadow-token"
        })
class PeopleInternalReadOperationalSmokeIntegrationTest {

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
        registry.add("people.shadow.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveComprovarSinaisDeSucessoNotFoundEErroNoHealthDoShadowReadOnly() {
        UUID tipoPessoaId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "codigo": "ALUNO",
                            "descricao": "Aluno"
                          }
                        ]
                        """.formatted(tipoPessoaId)));
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "content": [
                            {
                              "idAluno": "%s",
                              "nomeCompleto": "Aluno Smoke",
                              "cpf": "12345678901",
                              "email": "aluno.smoke@example.com",
                              "telefone": "11999999999",
                              "dataNascimento": "2014-03-10",
                              "createdAt": "2026-07-02T08:00:00",
                              "responsaveis": []
                            }
                          ],
                          "totalElements": 1,
                          "page": 0,
                          "size": 10
                        }
                        """.formatted(alunoId)));

        RestClient shadowClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-Internal-Token", "shadow-token")
                .defaultHeader("X-Correlation-Id", "corr-people-smoke")
                .defaultHeader("X-Usuario-Id", UUID.randomUUID().toString())
                .defaultHeader("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer shadow-user-token")
                .build();

        String tiposPessoa = shadowClient.get()
                .uri("/internal/v1/pessoas/catalogos/tipos-pessoa")
                .retrieve()
                .body(String.class);
        assertThat(tiposPessoa).contains("ALUNO");

        try {
            shadowClient.get()
                    .uri("/internal/v1/pessoas/{id}", pessoaId)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException exception) {
            assertThat(exception.getStatusCode().value()).isEqualTo(404);
        }

        try {
            shadowClient.get()
                    .uri("/internal/v1/pessoas/catalogos/tipos-endereco")
                    .retrieve()
                    .body(String.class);
        } catch (HttpServerErrorException exception) {
            assertThat(exception.getStatusCode().value()).isEqualTo(503);
        }

        String consulta = shadowClient.get()
                .uri("/internal/v1/pessoas/consulta-cadastral?nomeAluno=Aluno&page=0&size=10")
                .retrieve()
                .body(String.class);
        assertThat(consulta).contains("Aluno Smoke");

        RestClient actuatorClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<?, ?> health = actuatorClient.get()
                .uri("/actuator/health/peopleShadowMonolith")
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
        Map<String, Object> tiposPessoaRoute = (Map<String, Object>) shadowReadRoutes.get("listarTiposPessoa");
        @SuppressWarnings("unchecked")
        Map<String, Object> tiposEnderecoRoute = (Map<String, Object>) shadowReadRoutes.get("listarTiposEndereco");
        @SuppressWarnings("unchecked")
        Map<String, Object> buscarPorIdRoute = (Map<String, Object>) shadowReadRoutes.get("buscarPorId");
        @SuppressWarnings("unchecked")
        Map<String, Object> consultaRoute = (Map<String, Object>) shadowReadRoutes.get("consultarCadastro");

        assertThat(tiposPessoaRoute)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/catalogos/tipos-pessoa")
                .containsEntry("monolithSuccessTotal", 1.0d);
        assertThat(tiposEnderecoRoute)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/catalogos/tipos-endereco")
                .containsEntry("monolithErrorTotal", 1.0d)
                .containsEntry("failuresTotal", 1.0d);
        assertThat(buscarPorIdRoute)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/{id}")
                .containsEntry("monolithNotFoundTotal", 1.0d);
        assertThat(consultaRoute)
                .containsEntry("shadowRoute", "GET /internal/v1/pessoas/consulta-cadastral")
                .containsEntry("monolithSuccessTotal", 1.0d);
    }
}
