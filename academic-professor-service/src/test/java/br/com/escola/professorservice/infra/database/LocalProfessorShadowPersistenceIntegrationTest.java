package br.com.escola.professorservice.infra.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorTurmaShadowSyncStateJpaRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(properties = {
        "professor.shadow.internal-api.token=shadow-token",
        "professor.shadow.local-persistence.enabled=true",
        "management.endpoint.health.show-details=always"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LocalProfessorShadowPersistenceIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfessorShadowJpaRepository repository;

    @Autowired
    private ProfessorAlocacaoShadowJpaRepository alocacaoRepository;

    @Autowired
    private ProfessorAlocacaoShadowSyncStateJpaRepository alocacaoSyncStateRepository;

    @Autowired
    private ProfessorTurmaShadowSyncStateJpaRepository turmaSyncStateRepository;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        alocacaoSyncStateRepository.deleteAll();
        turmaSyncStateRepository.deleteAll();
        repository.deleteAll();
        alocacaoRepository.deleteAll();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("professor.shadow.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void devePersistirProfessorLocalmenteAposSucessoDoProxyShadow() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Persistido",
                          "escolaId": "%s",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-LOCAL",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-29T11:15:30",
                          "updatedAt": "2026-06-29T11:15:30"
                        }
                        """.formatted(professorId, pessoaId, escolaId)));

        mockMvc.perform(post("/internal/v1/professores")
                        .contentType("application/json")
                        .content("""
                                {
                                  "funcionarioId": "%s",
                                  "registroProfissional": "RP-LOCAL",
                                  "formacao": "Licenciatura",
                                  "ativo": true
                                }
                                """.formatted(funcionarioId))
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-local-create")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(professorId.toString()));

        ProfessorShadowJpaEntity persisted = repository.findById(professorId).orElseThrow();
        assertThat(persisted.getPessoaId()).isEqualTo(pessoaId);
        assertThat(persisted.getEscolaId()).isEqualTo(escolaId);
        assertThat(persisted.getNomeCompleto()).isEqualTo("Professor Persistido");

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details.enabled").value(true))
                .andExpect(jsonPath("$.details.createSuccessTotal").value(1.0))
                .andExpect(jsonPath("$.details.storedProfessorRecords").value(1))
                .andExpect(jsonPath("$.details.storedAllocationRecords").value(0))
                .andExpect(jsonPath("$.details.storedRecords").value(1));
    }

    @Test
    void devePersistirAlocacaoLocalmenteAposSucessoDoProxyShadow() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID professorId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        repository.save(new ProfessorShadowJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Professor Alocado",
                escolaId,
                "Escola Padrao",
                "RP-ALOC",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 11, 0, 0),
                LocalDateTime.of(2026, 6, 29, 11, 0, 0),
                null));

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody("""
                        {
                          "id": "%s",
                          "professorId": "%s",
                          "professorNome": "Professor Alocado",
                          "turmaDisciplinaId": "%s",
                          "turmaId": "%s",
                          "turmaNome": "Turma A",
                          "disciplinaId": "%s",
                          "disciplinaNome": "Matematica",
                          "dataInicio": "2026-02-01",
                          "dataFim": null,
                          "ativo": true,
                          "createdAt": "2026-06-29T11:30:00"
                        }
                        """.formatted(
                        alocacaoId,
                        professorId,
                        turmaDisciplinaId,
                        turmaId,
                        UUID.randomUUID())));

        mockMvc.perform(post("/internal/v1/professores/{id}/turmas-disciplinas", professorId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "turmaDisciplinaId": "%s",
                                  "dataInicio": "2026-02-01",
                                  "ativo": true
                                }
                                """.formatted(turmaDisciplinaId))
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-local-allocate")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(alocacaoId.toString()));

        assertThat(alocacaoRepository.findById(alocacaoId)).isPresent();
        assertThat(alocacaoSyncStateRepository.findById(professorId))
                .isPresent()
                .get()
                .satisfies(state -> {
                    assertThat(state.getEscolaId()).isEqualTo(escolaId);
                    assertThat(state.getAlocacoesCompletas()).isTrue();
                    assertThat(state.getAlocacaoCount()).isEqualTo(1L);
                });
        assertThat(turmaSyncStateRepository.findById(turmaId))
                .isPresent()
                .get()
                .satisfies(state -> {
                    assertThat(state.getEscolaId()).isEqualTo(escolaId);
                    assertThat(state.getAlocacoesCompletas()).isTrue();
                    assertThat(state.getAlocacaoCount()).isEqualTo(1L);
                });

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details.enabled").value(true))
                .andExpect(jsonPath("$.details.allocateSuccessTotal").value(1.0))
                .andExpect(jsonPath("$.details.storedProfessorRecords").value(1))
                .andExpect(jsonPath("$.details.storedAllocationRecords").value(1))
                .andExpect(jsonPath("$.details.storedRecords").value(2));
    }

    @Test
    void deveSinalizarDivergenciaSemQuebrarRespostaExternaQuandoPersistenciaLocalFalhaEmShadow() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID pessoaId = UUID.randomUUID();
        UUID professorLocalId = UUID.randomUUID();
        UUID professorRemotoId = UUID.randomUUID();

        repository.save(new ProfessorShadowJpaEntity(
                professorLocalId,
                pessoaId,
                "Professor Local Antigo",
                escolaId,
                "Escola Padrao",
                "RP-OLD",
                "Formacao Antiga",
                true,
                LocalDateTime.of(2026, 6, 29, 10, 0, 0),
                LocalDateTime.of(2026, 6, 29, 10, 0, 0),
                null));

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Divergente",
                          "escolaId": "%s",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-DIV",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-29T12:00:00",
                          "updatedAt": "2026-06-29T12:00:00"
                        }
                        """.formatted(professorRemotoId, pessoaId, escolaId)));

        mockMvc.perform(post("/internal/v1/professores")
                        .contentType("application/json")
                        .content("""
                                {
                                  "funcionarioId": "%s",
                                  "registroProfissional": "RP-DIV",
                                  "formacao": "Licenciatura",
                                  "ativo": true
                                }
                                """.formatted(UUID.randomUUID()))
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-divergence")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(professorRemotoId.toString()));

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findById(professorLocalId)).isPresent();
        assertThat(repository.findById(professorRemotoId)).isEmpty();

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"))
                .andExpect(jsonPath("$.details.enabled").value(true))
                .andExpect(jsonPath("$.details.createDivergenceTotal").value(1.0))
                .andExpect(jsonPath("$.details.failuresTotal").value(1.0))
                .andExpect(jsonPath("$.details.storedProfessorRecords").value(1))
                .andExpect(jsonPath("$.details.storedAllocationRecords").value(0))
                .andExpect(jsonPath("$.details.storedRecords").value(1));
    }

    @Test
    void deveSinalizarDivergenciaDeAlocacaoSemQuebrarRespostaExternaQuandoProfessorNaoExisteNaShadow() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID professorId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody("""
                        {
                          "id": "%s",
                          "professorId": "%s",
                          "professorNome": "Professor Sem Shadow",
                          "turmaDisciplinaId": "%s",
                          "turmaId": "%s",
                          "turmaNome": "Turma Divergente",
                          "disciplinaId": "%s",
                          "disciplinaNome": "Historia",
                          "dataInicio": "2026-02-01",
                          "dataFim": null,
                          "ativo": true,
                          "createdAt": "2026-06-29T12:30:00"
                        }
                        """.formatted(
                        UUID.randomUUID(),
                        professorId,
                        turmaDisciplinaId,
                        turmaId,
                        UUID.randomUUID())));

        mockMvc.perform(post("/internal/v1/professores/{id}/turmas-disciplinas", professorId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "turmaDisciplinaId": "%s",
                                  "dataInicio": "2026-02-01",
                                  "ativo": true
                                }
                                """.formatted(turmaDisciplinaId))
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-allocate-divergence")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorId").value(professorId.toString()));

        assertThat(alocacaoRepository.count()).isZero();
        assertThat(alocacaoSyncStateRepository.findById(professorId)).isEmpty();
        assertThat(turmaSyncStateRepository.findById(turmaId)).isEmpty();

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"))
                .andExpect(jsonPath("$.details.enabled").value(true))
                .andExpect(jsonPath("$.details.allocateDivergenceTotal").value(1.0))
                .andExpect(jsonPath("$.details.failuresTotal").value(1.0))
                .andExpect(jsonPath("$.details.storedProfessorRecords").value(0))
                .andExpect(jsonPath("$.details.storedAllocationRecords").value(0))
                .andExpect(jsonPath("$.details.storedRecords").value(0));
    }
}
