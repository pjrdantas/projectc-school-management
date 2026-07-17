package br.com.escola.professorservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDate;
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

import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;
import br.com.escola.professorservice.infra.database.entity.TurmaSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import br.com.escola.professorservice.infra.database.repository.AlocacaoJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.repository.TurmaSyncStateJpaRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(properties = {
        "professor.shadow.local-persistence.enabled=true",
        "professor.shadow.local-persistence.listar-por-turma-cutover-enabled=true",
        "management.endpoint.health.show-details=always"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ConsultaInternaControllerListarPorTurmaCutoverIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CadastroJpaRepository professorRepository;

    @Autowired
    private AlocacaoJpaRepository alocacaoRepository;

    @Autowired
    private TurmaSyncStateJpaRepository turmaSyncStateRepository;

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
        turmaSyncStateRepository.deleteAll();
        alocacaoRepository.deleteAll();
        professorRepository.deleteAll();
    }

    @Test
    void deveConsultarProfessoresPorTurmaLocalmenteSemFallbackQuandoSyncDaTurmaEstaCompleto() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID professorId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        int requestCountBefore = mockWebServer.getRequestCount();

        professorRepository.save(new CadastroJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Professor Local Turma",
                escolaId,
                "Escola Padrao",
                "RP-TURMA-CUTOVER",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 7, 2, 9, 0, 0),
                LocalDateTime.of(2026, 7, 2, 9, 0, 0),
                null));
        alocacaoRepository.save(new AlocacaoJpaEntity(
                UUID.randomUUID(),
                professorId,
                UUID.randomUUID(),
                turmaId,
                "Turma Local Cutover",
                UUID.randomUUID(),
                "Geografia",
                LocalDate.of(2026, 2, 1),
                null,
                true,
                LocalDateTime.of(2026, 7, 2, 9, 5, 0)));
        turmaSyncStateRepository.save(new TurmaSyncStateJpaEntity(
                turmaId,
                escolaId,
                true,
                1L,
                LocalDateTime.of(2026, 7, 2, 9, 6, 0)));

        mockMvc.perform(get("/internal/v1/turmas/{turmaId}/professores", turmaId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-cutover-local-turma")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$[0].professorNome").value("Professor Local Turma"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);
    }

    @Test
    void deveResponder503SemConsultarMonolitoQuandoCutoverPorTurmaEstaAtivoESyncAindaNaoEstaCompleto()
            throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID turmaId = UUID.randomUUID();
        int requestCountBefore = mockWebServer.getRequestCount();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "professorId": "%s",
                            "professorNome": "Nao Deve Consultar Monolito",
                            "turmaDisciplinaId": "%s",
                            "turmaId": "%s",
                            "turmaNome": "Turma Monolito",
                            "disciplinaId": "%s",
                            "disciplinaNome": "Historia",
                            "dataInicio": "2026-02-01",
                            "dataFim": null,
                            "ativo": true,
                            "createdAt": "2026-07-02T09:10:00"
                          }
                        ]
                        """.formatted(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        turmaId,
                        UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/turmas/{turmaId}/professores", turmaId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-cutover-blocked-turma")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("DOWNSTREAM_UNAVAILABLE"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.listarPorTurmaCutoverEnabled").value(true))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.readStrategy")
                        .value("complete_sync_state_required_no_fallback"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.cutoverEnabled").value(true))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.rollbackStrategy")
                        .value("disable_property"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.localCutoverBlockedTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.fallbackTotal").value(0.0));
    }
}
