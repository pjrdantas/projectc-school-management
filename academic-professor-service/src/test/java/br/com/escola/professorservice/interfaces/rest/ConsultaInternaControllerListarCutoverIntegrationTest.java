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

import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroSyncStateJpaRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(properties = {
        "professor.shadow.local-persistence.enabled=true",
        "professor.shadow.local-persistence.listar-cutover-enabled=true",
        "management.endpoint.health.show-details=always"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ConsultaInternaControllerListarCutoverIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CadastroJpaRepository professorRepository;

    @Autowired
    private CadastroSyncStateJpaRepository syncStateRepository;

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
        syncStateRepository.deleteAll();
        professorRepository.deleteAll();
    }

    @Test
    void deveListarProfessoresLocalmenteSemFallbackQuandoSyncDaEscolaEstaCompleto() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        int requestCountBefore = mockWebServer.getRequestCount();

        professorRepository.save(new CadastroJpaEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Professor Local Lista",
                escolaId,
                "Escola Padrao",
                "RP-LISTA-CUTOVER",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 7, 3, 9, 0, 0),
                LocalDateTime.of(2026, 7, 3, 9, 0, 0),
                null));
        syncStateRepository.save(new CadastroSyncStateJpaEntity(
                escolaId,
                true,
                1L,
                LocalDateTime.of(2026, 7, 3, 9, 5, 0)));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-cutover-local-lista")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor Local Lista"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);
    }

    @Test
    void deveResponder503SemConsultarMonolitoQuandoCutoverDaListaEstaAtivoESyncAindaNaoEstaCompleto()
            throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        int requestCountBefore = mockWebServer.getRequestCount();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Nao Deve Consultar Monolito",
                            "escolaId": "%s",
                            "escolaNome": "Escola Padrao",
                            "registroProfissional": "RP-MONO",
                            "formacao": "Licenciatura",
                            "ativo": true,
                            "createdAt": "2026-07-03T09:10:00",
                            "updatedAt": "2026-07-03T09:10:00"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID(), escolaId)));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-cutover-blocked-lista")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("DOWNSTREAM_UNAVAILABLE"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.listarCutoverEnabled").value(true))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.readStrategy")
                        .value("complete_sync_state_required_no_fallback"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.cutoverEnabled").value(true))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.rollbackStrategy")
                        .value("disable_property"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.localCutoverBlockedTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.fallbackTotal").value(0.0));
    }
}
