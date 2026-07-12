package br.com.escola.professorservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(properties = {
        "professor.shadow.local-persistence.enabled=true",
        "professor.shadow.local-persistence.buscar-por-id-cutover-enabled=true",
        "management.endpoint.health.show-details=always"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProfessorShadowQueryControllerBuscarPorIdCutoverIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfessorShadowJpaRepository professorRepository;

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
        registry.add("professor.shadow.internal-api.token", () -> "shadow-token");
        registry.add("professor.shadow.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @BeforeEach
    void setUp() {
        professorRepository.deleteAll();
    }

    @Test
    void deveConsultarProfessorPorIdDoBancoLocalSemFallbackQuandoCutoverControladoEstaAtivo() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID professorId = UUID.randomUUID();
        int requestCountBefore = mockWebServer.getRequestCount();

        professorRepository.save(new ProfessorShadowJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Professor Local Cutover",
                escolaId,
                "Escola Padrao",
                "RP-CUTOVER",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 11, 0, 0),
                LocalDateTime.of(2026, 6, 29, 11, 5, 0),
                null));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-cutover-local-id")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Local Cutover"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);
    }

    @Test
    void deveResponder404SemConsultarMonolitoQuandoProfessorNaoExisteLocalmenteEOCutoverEstaAtivo() throws Exception {
        UUID professorId = UUID.randomUUID();
        int requestCountBefore = mockWebServer.getRequestCount();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Nao Deve Ser Consultado",
                          "escolaId": "00000000-0000-0000-0000-000000000047",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-MONO",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-29T11:10:00",
                          "updatedAt": "2026-06-29T11:10:00"
                        }
                        """.formatted(professorId, UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-cutover-missing-id")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.buscarPorIdCutoverEnabled").value(true))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.readStrategy")
                        .value("local_record_presence_required_no_fallback"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.cutoverEnabled").value(true))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.rollbackStrategy").value("disable_property"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.localCutoverNotFoundTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.fallbackTotal").value(0.0));
    }
}
