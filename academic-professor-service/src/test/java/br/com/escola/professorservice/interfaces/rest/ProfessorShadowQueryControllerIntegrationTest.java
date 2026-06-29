package br.com.escola.professorservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowSyncStateJpaRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest(properties = "professor.shadow.local-persistence.enabled=true")
@AutoConfigureMockMvc
class ProfessorShadowQueryControllerIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfessorShadowJpaRepository professorRepository;

    @Autowired
    private ProfessorAlocacaoShadowJpaRepository alocacaoRepository;

    @Autowired
    private ProfessorShadowSyncStateJpaRepository syncStateRepository;

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
        alocacaoRepository.deleteAll();
        syncStateRepository.deleteAll();
        professorRepository.deleteAll();
    }

    @Test
    void deveListarProfessoresDoBancoLocalQuandoEscolaJaEstaMarcadaComoCompletaNaShadow() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        int requestCountBefore = mockWebServer.getRequestCount();

        professorRepository.save(new ProfessorShadowJpaEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Professor A Local",
                escolaId,
                "Escola Padrao",
                "RP-A-LOCAL",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 7, 0, 0),
                LocalDateTime.of(2026, 6, 29, 7, 15, 0),
                null));
        professorRepository.save(new ProfessorShadowJpaEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Professor B Local",
                escolaId,
                "Escola Padrao",
                "RP-B-LOCAL",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 7, 20, 0),
                LocalDateTime.of(2026, 6, 29, 7, 25, 0),
                null));
        syncStateRepository.save(new ProfessorShadowSyncStateJpaEntity(
                escolaId,
                true,
                2L,
                LocalDateTime.of(2026, 6, 29, 7, 30, 0)));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-local-listar")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor A Local"))
                .andExpect(jsonPath("$[1].nomeCompleto").value("Professor B Local"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);
    }

    @Test
    void deveFazerFallbackAoMonolitoQuandoListaLocalAindaNaoTemMarcacaoDeCompletude() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        professorRepository.save(new ProfessorShadowJpaEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Professor Parcial Local",
                escolaId,
                "Escola Padrao",
                "RP-PARCIAL",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 7, 40, 0),
                LocalDateTime.of(2026, 6, 29, 7, 45, 0),
                null));

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Professor Fallback Lista",
                            "escolaId": "00000000-0000-0000-0000-000000000047",
                            "escolaNome": "Escola Padrao",
                            "registroProfissional": "RP-FALLBACK-LISTA",
                            "formacao": "Licenciatura",
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30",
                            "updatedAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-fallback-listar")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor Fallback Lista"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/professores");
        assertThat(recorded.getPath()).isEqualTo("/internal/professores");
    }

    @Test
    void deveCriarProfessorNoRuntimeShadow() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Shadow Write",
                          "escolaId": "00000000-0000-0000-0000-000000000047",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-WRITE",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-23T10:15:30",
                          "updatedAt": "2026-06-23T10:15:30"
                        }
                        """.formatted(professorId, UUID.randomUUID())));

        mockMvc.perform(post("/internal/v1/professores")
                        .contentType("application/json")
                        .content("""
                                {
                                  "funcionarioId": "%s",
                                  "registroProfissional": "RP-WRITE",
                                  "formacao": "Licenciatura",
                                  "ativo": true
                                }
                                """.formatted(funcionarioId))
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-write")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Shadow Write"));

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getPath()).isEqualTo("/internal/professores");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer shadow-user-token");
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(recorded.getBody().readUtf8()).contains(funcionarioId.toString());
    }

    @Test
    void deveAlocarProfessorNoRuntimeShadow() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody("""
                        {
                          "id": "%s",
                          "professorId": "%s",
                          "professorNome": "Professor Shadow Write",
                          "turmaDisciplinaId": "%s",
                          "turmaId": "%s",
                          "turmaNome": "Turma Write",
                          "disciplinaId": "%s",
                          "disciplinaNome": "Matematica",
                          "dataInicio": "2026-02-01",
                          "dataFim": null,
                          "ativo": true,
                          "createdAt": "2026-06-23T10:15:30"
                        }
                        """.formatted(UUID.randomUUID(), professorId, turmaDisciplinaId, turmaId, disciplinaId)));

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
                        .header("X-Correlation-Id", "corr-shadow-allocate")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.turmaDisciplinaId").value(turmaDisciplinaId.toString()))
                .andExpect(jsonPath("$.disciplinaNome").value("Matematica"));

        RecordedRequest recorded = aguardarRequisicao("POST", "/internal/professores/" + professorId + "/turmas-disciplinas");
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getPath()).isEqualTo("/internal/professores/" + professorId + "/turmas-disciplinas");
        assertThat(recorded.getBody().readUtf8()).contains(turmaDisciplinaId.toString());
    }

    @Test
    void deveConsultarProfessorPorIdNoRuntimeShadow() throws Exception {
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Shadow",
                          "escolaId": "00000000-0000-0000-0000-000000000047",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-SHADOW",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-23T10:15:30",
                          "updatedAt": "2026-06-23T10:15:30"
                        }
                        """.formatted(professorId, UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Shadow"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/professores/" + professorId);
        assertThat(recorded.getMethod()).isEqualTo("GET");
        assertThat(recorded.getPath()).isEqualTo("/internal/professores/" + professorId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer shadow-user-token");
        assertThat(recorded.getHeader("X-Escola-Id")).isEqualTo("00000000-0000-0000-0000-000000000047");
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-shadow-1");
    }

    @Test
    void deveConsultarProfessorPorIdDoBancoLocalQuandoShadowJaPossuiOCadastro() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID professorId = UUID.randomUUID();
        int requestCountBefore = mockWebServer.getRequestCount();

        professorRepository.save(new ProfessorShadowJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Professor Local Id",
                escolaId,
                "Escola Padrao",
                "RP-LOCAL-ID",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 8, 0, 0),
                LocalDateTime.of(2026, 6, 29, 8, 30, 0),
                null));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-local-id")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Local Id"))
                .andExpect(jsonPath("$.registroProfissional").value("RP-LOCAL-ID"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);
    }

    @Test
    void deveFazerFallbackAoMonolitoQuandoProfessorNaoExisteNaShadowLocal() throws Exception {
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Fallback Id",
                          "escolaId": "00000000-0000-0000-0000-000000000047",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-FALLBACK-ID",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-23T10:15:30",
                          "updatedAt": "2026-06-23T10:15:30"
                        }
                        """.formatted(professorId, UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-fallback-id")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Fallback Id"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/professores/" + professorId);
        assertThat(recorded.getPath()).isEqualTo("/internal/professores/" + professorId);
    }

    @Test
    void deveListarAlocacoesDoBancoLocalQuandoProfessorJaExisteNaShadow() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID professorId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        int requestCountBefore = mockWebServer.getRequestCount();

        professorRepository.save(new ProfessorShadowJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Professor Local Read",
                escolaId,
                "Escola Padrao",
                "RP-LOCAL-READ",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 9, 0, 0),
                LocalDateTime.of(2026, 6, 29, 9, 0, 0),
                null));
        alocacaoRepository.save(new ProfessorAlocacaoShadowJpaEntity(
                UUID.randomUUID(),
                professorId,
                turmaDisciplinaId,
                turmaId,
                "Turma Local",
                disciplinaId,
                "Matematica",
                LocalDate.of(2026, 2, 1),
                null,
                true,
                LocalDateTime.of(2026, 6, 29, 9, 30, 0)));

        mockMvc.perform(get("/internal/v1/professores/{id}/turmas-disciplinas", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-local-read-alocacoes")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].professorNome").value("Professor Local Read"))
                .andExpect(jsonPath("$[0].turmaDisciplinaId").value(turmaDisciplinaId.toString()))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Matematica"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);
    }

    @Test
    void deveListarProfessoresPorTurmaDoBancoLocalQuandoJaExistiremAlocacoesNaShadow() throws Exception {
        UUID escolaId = UUID.fromString("00000000-0000-0000-0000-000000000047");
        UUID professorId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        int requestCountBefore = mockWebServer.getRequestCount();

        professorRepository.save(new ProfessorShadowJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Professor Turma Local",
                escolaId,
                "Escola Padrao",
                "RP-TURMA",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 10, 0, 0),
                LocalDateTime.of(2026, 6, 29, 10, 0, 0),
                null));
        alocacaoRepository.save(new ProfessorAlocacaoShadowJpaEntity(
                UUID.randomUUID(),
                professorId,
                UUID.randomUUID(),
                turmaId,
                "Turma 2A",
                UUID.randomUUID(),
                "Historia",
                LocalDate.of(2026, 2, 1),
                null,
                true,
                LocalDateTime.of(2026, 6, 29, 10, 15, 0)));

        mockMvc.perform(get("/internal/v1/turmas/{turmaId}/professores", turmaId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-local-read-turma")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].professorNome").value("Professor Turma Local"))
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Historia"));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(requestCountBefore);
    }

    @Test
    void deveFazerFallbackAoMonolitoQuandoLeituraLocalPorTurmaAindaNaoTemBaseSuficiente() throws Exception {
        UUID turmaId = UUID.randomUUID();
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
                            "createdAt": "2026-06-23T10:15:30"
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
                        .header("X-Correlation-Id", "corr-shadow-fallback-turma")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorNome").value("Professor Fallback Turma"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/professores/turmas/" + turmaId);
        assertThat(recorded.getPath()).isEqualTo("/internal/professores/turmas/" + turmaId);
    }

    @Test
    void deveListarProfessoresAlocacoesEFuncionariosElegiveisNoRuntimeShadow() throws Exception {
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Professor Shadow Lista",
                            "escolaId": "00000000-0000-0000-0000-000000000047",
                            "escolaNome": "Escola Padrao",
                            "registroProfissional": "RP-LISTA",
                            "formacao": "Licenciatura",
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30",
                            "updatedAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "professorId": "%s",
                            "professorNome": "Professor Shadow",
                            "turmaDisciplinaId": "%s",
                            "turmaId": "%s",
                            "turmaNome": "1A",
                            "disciplinaId": "%s",
                            "disciplinaNome": "Matematica",
                            "dataInicio": "2026-02-01",
                            "dataFim": null,
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(
                        UUID.randomUUID(),
                        professorId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "funcionarioId": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Funcionario Elegivel Shadow",
                            "escolaId": "00000000-0000-0000-0000-000000000047",
                            "escolaNome": "Escola Padrao",
                            "cargo": "Professor",
                            "ativo": true,
                            "elegivelProfessor": true
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-2a")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor Shadow Lista"));

        mockMvc.perform(get("/internal/v1/professores/{id}/turmas-disciplinas", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Matematica"));

        mockMvc.perform(get("/internal/v1/professores/funcionarios-elegiveis")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Funcionario Elegivel Shadow"))
                .andExpect(jsonPath("$[0].elegivelProfessor").value(true));
    }

    @Test
    void deveResponderRotasInternasCompativeisComOMonolitoAtual() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "pessoaId": "%s",
                            "nomeCompleto": "Professor Compat",
                            "escolaId": "00000000-0000-0000-0000-000000000047",
                            "escolaNome": "Escola Padrao",
                            "registroProfissional": "RP-COMPAT",
                            "formacao": "Licenciatura",
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30",
                            "updatedAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "%s",
                          "pessoaId": "%s",
                          "nomeCompleto": "Professor Compat",
                          "escolaId": "00000000-0000-0000-0000-000000000047",
                          "escolaNome": "Escola Padrao",
                          "registroProfissional": "RP-COMPAT",
                          "formacao": "Licenciatura",
                          "ativo": true,
                          "createdAt": "2026-06-23T10:15:30",
                          "updatedAt": "2026-06-23T10:15:30"
                        }
                        """.formatted(professorId, UUID.randomUUID())));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "professorId": "%s",
                            "professorNome": "Professor Compat",
                            "turmaDisciplinaId": "%s",
                            "turmaId": "%s",
                            "turmaNome": "2A",
                            "disciplinaId": "%s",
                            "disciplinaNome": "Geografia",
                            "dataInicio": "2026-02-01",
                            "dataFim": null,
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(
                        UUID.randomUUID(),
                        professorId,
                        UUID.randomUUID(),
                        turmaId,
                        UUID.randomUUID())));

        mockMvc.perform(get("/internal/professores")
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-compat-list")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor Compat"));

        mockMvc.perform(get("/internal/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-compat-id")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()));

        mockMvc.perform(get("/internal/professores/turmas/{turmaId}", turmaId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-compat-turma")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()));
    }

    @Test
    void deveListarProfessoresPorTurmaNoRuntimeShadow() throws Exception {
        UUID turmaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id": "%s",
                            "professorId": "%s",
                            "professorNome": "Professor Shadow Turma",
                            "turmaDisciplinaId": "%s",
                            "turmaId": "%s",
                            "turmaNome": "1B",
                            "disciplinaId": "%s",
                            "disciplinaNome": "Historia",
                            "dataInicio": "2026-02-01",
                            "dataFim": null,
                            "ativo": true,
                            "createdAt": "2026-06-23T10:15:30"
                          }
                        ]
                        """.formatted(
                        UUID.randomUUID(),
                        professorId,
                        UUID.randomUUID(),
                        turmaId,
                        UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/turmas/{turmaId}/professores", turmaId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-3b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Historia"));

        RecordedRequest recorded = aguardarRequisicao("GET", "/internal/professores/turmas/" + turmaId);
        assertThat(recorded.getPath()).isEqualTo("/internal/professores/turmas/" + turmaId);
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/professores/funcionarios-elegiveis")
                        .header("X-Correlation-Id", "corr-shadow-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveMapearProfessorNaoEncontrado() throws Exception {
        UUID professorId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "shadow-token")
                        .header("X-Correlation-Id", "corr-shadow-5")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", "00000000-0000-0000-0000-000000000047")
                        .header("Authorization", "Bearer shadow-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    private RecordedRequest aguardarRequisicao(String method, String path) throws InterruptedException {
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
            if (request == null) {
                continue;
            }
            if (method.equals(request.getMethod()) && path.equals(request.getPath())) {
                return request;
            }
        }
        throw new AssertionError("Requisicao esperada nao encontrada: " + method + " " + path);
    }
}
