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

import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;
import br.com.escola.professorservice.infra.database.entity.AlocacaoSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.TurmaSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.AlocacaoSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.AlocacaoJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.TurmaSyncStateJpaRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(properties = {
        "professor.shadow.internal-api.token=shadow-token",
        "professor.shadow.local-persistence.enabled=true",
        "management.endpoint.health.show-details=always"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PersistenciaLocalIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CadastroJpaRepository repository;

    @Autowired
    private AlocacaoJpaRepository alocacaoRepository;

    @Autowired
    private AlocacaoSyncStateJpaRepository alocacaoSyncStateRepository;

    @Autowired
    private TurmaSyncStateJpaRepository turmaSyncStateRepository;

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

    @BeforeEach
    void setUp() {
        alocacaoSyncStateRepository.deleteAll();
        turmaSyncStateRepository.deleteAll();
        syncStateRepository.deleteAll();
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

        CadastroJpaEntity persisted = repository.findById(professorId).orElseThrow();
        assertThat(persisted.getPessoaId()).isEqualTo(pessoaId);
        assertThat(persisted.getEscolaId()).isEqualTo(escolaId);
        assertThat(persisted.getNomeCompleto()).isEqualTo("Professor Persistido");
        syncStateRepository.save(new CadastroSyncStateJpaEntity(
                escolaId,
                true,
                1L,
                LocalDateTime.of(2026, 6, 29, 11, 16, 0)));

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details.enabled").value(true))
                .andExpect(jsonPath("$.details.createSuccessTotal").value(1.0))
                .andExpect(jsonPath("$.details.storedProfessorRecords").value(1))
                .andExpect(jsonPath("$.details.storedAllocationRecords").value(0))
                .andExpect(jsonPath("$.details.storedRecords").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.professores.trackedTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.professores.completeTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.professores.trackedRecordsTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.professores.lastSynchronizedAt")
                        .value("2026-06-29T11:16"))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorProfessor.trackedTotal").value(0))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorTurma.trackedTotal").value(0));
    }

    @Test
    void devePersistirAlocacaoLocalmenteAposSucessoDoProxyShadow() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID professorId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        repository.save(new CadastroJpaEntity(
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
                .andExpect(jsonPath("$.details.storedRecords").value(2))
                .andExpect(jsonPath("$.details.shadowSyncStates.professores.trackedTotal").value(0))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorProfessor.trackedTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorProfessor.completeTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorProfessor.trackedRecordsTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorProfessor.lastSynchronizedAt")
                        .value("2026-06-29T11:30"))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorTurma.trackedTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorTurma.completeTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorTurma.trackedRecordsTotal").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorTurma.lastSynchronizedAt")
                        .value("2026-06-29T11:30"));
    }

    @Test
    void deveSinalizarDivergenciaSemQuebrarRespostaExternaQuandoPersistenciaLocalFalhaEmShadow() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID pessoaId = UUID.randomUUID();
        UUID professorLocalId = UUID.randomUUID();
        UUID professorRemotoId = UUID.randomUUID();

        repository.save(new CadastroJpaEntity(
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
                .andExpect(jsonPath("$.details.storedRecords").value(1))
                .andExpect(jsonPath("$.details.shadowSyncStates.professores.trackedTotal").value(0))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorProfessor.trackedTotal").value(0))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorTurma.trackedTotal").value(0));
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
                .andExpect(jsonPath("$.details.storedRecords").value(0))
                .andExpect(jsonPath("$.details.shadowSyncStates.professores.trackedTotal").value(0))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorProfessor.trackedTotal").value(0))
                .andExpect(jsonPath("$.details.shadowSyncStates.alocacoesPorTurma.trackedTotal").value(0));
    }

    @Test
    void deveExporRotasDeLeituraComFallbackOuUsoLocalPorCompletudeDaShadow() throws Exception {
        UUID escolaCompletaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID escolaParcialId = UUID.fromString("00000000-0000-0000-0000-000000000048");
        UUID professorLocalId = UUID.randomUUID();
        UUID professorFallbackId = UUID.randomUUID();
        UUID professorAlocacaoFallbackId = UUID.randomUUID();
        UUID turmaLocalId = UUID.randomUUID();
        UUID turmaFallbackId = UUID.randomUUID();

        repository.save(new CadastroJpaEntity(
                professorLocalId,
                UUID.randomUUID(),
                "Professor Local Completo",
                escolaCompletaId,
                "Escola Completa",
                "RP-LOCAL-COMP",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 13, 0, 0),
                LocalDateTime.of(2026, 6, 29, 13, 0, 0),
                null));
        syncStateRepository.save(new CadastroSyncStateJpaEntity(
                escolaCompletaId,
                true,
                1L,
                LocalDateTime.of(2026, 6, 29, 13, 1, 0)));
        repository.save(new CadastroJpaEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Professor Parcial",
                escolaParcialId,
                "Escola Parcial",
                "RP-PARCIAL",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 13, 2, 0),
                LocalDateTime.of(2026, 6, 29, 13, 2, 0),
                null));

        alocacaoRepository.save(new AlocacaoJpaEntity(
                UUID.randomUUID(),
                professorLocalId,
                UUID.randomUUID(),
                turmaLocalId,
                "Turma Local",
                UUID.randomUUID(),
                "Matematica",
                java.time.LocalDate.of(2026, 2, 1),
                null,
                true,
                LocalDateTime.of(2026, 6, 29, 13, 3, 0)));
        alocacaoSyncStateRepository.save(new AlocacaoSyncStateJpaEntity(
                professorLocalId,
                escolaCompletaId,
                true,
                1L,
                LocalDateTime.of(2026, 6, 29, 13, 4, 0)));
        turmaSyncStateRepository.save(new TurmaSyncStateJpaEntity(
                turmaLocalId,
                escolaCompletaId,
                true,
                1L,
                LocalDateTime.of(2026, 6, 29, 13, 5, 0)));

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Professor Fallback Lista",
                            "escolaId": "%s",
                            "escolaNome": "Escola Parcial",
                            "registroProfissional": "RP-FALLBACK-LISTA",
                            "formacao": "Licenciatura",
                            "ativo": true,
                            "createdAt": "2026-06-29T13:10:00",
                            "updatedAt": "2026-06-29T13:10:00"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID(), escolaParcialId)));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Fallback Id",
                          "escolaId": "%s",
                          "escolaNome": "Escola Completa",
                          "registroProfissional": "RP-FALLBACK-ID",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-29T13:11:00",
                          "updatedAt": "2026-06-29T13:11:00"
                        }
                        """.formatted(professorFallbackId, UUID.randomUUID(), escolaCompletaId)));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "professorId": "%s",
                            "professorNome": "Professor Fallback Alocacao",
                            "turmaDisciplinaId": "%s",
                            "turmaId": "%s",
                            "turmaNome": "Turma Fallback Professor",
                            "disciplinaId": "%s",
                            "disciplinaNome": "Historia",
                            "dataInicio": "2026-02-01",
                            "dataFim": null,
                            "ativo": true,
                            "createdAt": "2026-06-29T13:12:00"
                          }
                        ]
                        """.formatted(
                        UUID.randomUUID(),
                        professorAlocacaoFallbackId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "professorId": "%s",
                            "professorNome": "Professor Fallback Turma",
                            "turmaDisciplinaId": "%s",
                            "turmaId": "%s",
                            "turmaNome": "Turma Fallback",
                            "disciplinaId": "%s",
                            "disciplinaNome": "Geografia",
                            "dataInicio": "2026-02-01",
                            "dataFim": null,
                            "ativo": true,
                            "createdAt": "2026-06-29T13:13:00"
                          }
                        ]
                        """.formatted(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        turmaFallbackId,
                        UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-health-local-list")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaCompletaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor Local Completo"));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-health-fallback-list")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaParcialId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor Fallback Lista"));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorLocalId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-health-local-id")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaCompletaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Local Completo"));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorFallbackId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-health-fallback-id")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaCompletaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Fallback Id"));

        mockMvc.perform(get("/internal/v1/professores/{id}/turmas-disciplinas", professorLocalId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-health-local-aloc")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaCompletaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorLocalId.toString()));

        mockMvc.perform(get("/internal/v1/professores/{id}/turmas-disciplinas", professorAlocacaoFallbackId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-health-fallback-aloc")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaCompletaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorNome").value("Professor Fallback Alocacao"));

        mockMvc.perform(get("/internal/v1/turmas/{turmaId}/professores", turmaLocalId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-health-local-turma")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaCompletaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].turmaId").value(turmaLocalId.toString()));

        mockMvc.perform(get("/internal/v1/turmas/{turmaId}/professores", turmaFallbackId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-health-fallback-turma")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaCompletaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorNome").value("Professor Fallback Turma"));

        mockMvc.perform(get("/actuator/health/professorShadowPersistence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.buscarPorIdCutoverEnabled").value(false))
                .andExpect(jsonPath("$.details.listarAlocacoesCutoverEnabled").value(false))
                .andExpect(jsonPath("$.details.listarPorTurmaCutoverEnabled").value(false))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.localTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.fallbackTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.fallbackIncompleteSyncStateTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.localSyncReadyTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listar.syncStateSummary.completeTotal").value(1))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.localTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.fallbackTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.cutoverEnabled").value(false))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.rollbackStrategy").value("disable_property"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.localCutoverNotFoundTotal").value(0.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.localRecordPresentTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.buscarPorId.fallbackMissingLocalRecordTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarAlocacoes.localTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarAlocacoes.fallbackTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarAlocacoes.cutoverEnabled").value(false))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarAlocacoes.rollbackStrategy").value("disable_property"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarAlocacoes.localCutoverBlockedTotal").value(0.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarAlocacoes.fallbackIncompleteSyncStateTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarAlocacoes.syncStateSummary.completeTotal").value(1))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.localTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.fallbackTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.cutoverEnabled").value(false))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.rollbackStrategy").value("disable_property"))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.localCutoverBlockedTotal").value(0.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.fallbackIncompleteSyncStateTotal").value(1.0))
                .andExpect(jsonPath("$.details.shadowReadRoutes.listarPorTurma.syncStateSummary.completeTotal").value(1));
    }
}

