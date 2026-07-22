package br.com.escola.professorservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
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

import br.com.escola.professorservice.infra.database.repository.AlocacaoJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest(properties = "professor.internal-api.token=professor-token")
@AutoConfigureMockMvc
class ConsultaInternaControllerIntegrationTest {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    private static MockWebServer peopleServer;
    private static MockWebServer catalogServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CadastroJpaRepository cadastroRepository;

    @Autowired
    private AlocacaoJpaRepository alocacaoRepository;

    @BeforeAll
    static void beforeAll() throws IOException {
        peopleServer = new MockWebServer();
        catalogServer = new MockWebServer();
        peopleServer.start();
        catalogServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        peopleServer.shutdown();
        catalogServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("professor.people-service.base-url", () -> peopleServer.url("/").toString());
        registry.add("professor.people-service.internal-token", () -> "people-token");
        registry.add("professor.catalog-service.base-url", () -> catalogServer.url("/").toString());
        registry.add("professor.catalog-service.internal-token", () -> "catalog-token");
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        alocacaoRepository.deleteAll();
        cadastroRepository.deleteAll();
        drainRequests(peopleServer);
        drainRequests(catalogServer);
    }

    @Test
    void deveOperarProfessorComoOwnerLocalSemMonolito() throws Exception {
        UUID funcionarioId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID professorId;
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        peopleServer.enqueue(json("""
                {
                  "funcionarioId": "%s",
                  "pessoaId": "%s",
                  "escolaId": "%s",
                  "nomeCompleto": "Ana Professor",
                  "cargoDescricao": "Coordenadora",
                  "ativo": true
                }
                """.formatted(funcionarioId, pessoaId, ESCOLA_ID)));
        peopleServer.enqueue(json("""
                {
                  "id": "%s",
                  "nomeCompleto": "Ana Professor",
                  "escolaId": "%s",
                  "escolaNome": "Escola Central",
                  "ativo": true
                }
                """.formatted(pessoaId, ESCOLA_ID)));

        var createResult = mockMvc.perform(post("/internal/v1/professores")
                        .contentType("application/json")
                        .content("""
                                {
                                  "funcionarioId": "%s",
                                  "registroProfissional": "RP-123",
                                  "formacao": "Licenciatura",
                                  "ativo": true
                                }
                                """.formatted(funcionarioId))
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-create")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCompleto").value("Ana Professor"))
                .andExpect(jsonPath("$.escolaNome").value("Escola Central"))
                .andReturn();

        professorId = UUID.fromString(com.jayway.jsonpath.JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.id"));

        mockMvc.perform(get("/internal/v1/professores")
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-list")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(professorId.toString()))
                .andExpect(jsonPath("$[0].nomeCompleto").value("Ana Professor"));

        mockMvc.perform(get("/internal/v1/professores/{id}", professorId)
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-id")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registroProfissional").value("RP-123"));

        catalogServer.enqueue(json("""
                {
                  "id": "%s",
                  "turmaId": "%s",
                  "disciplinaId": "%s",
                  "disciplinaNome": "Matematica",
                  "cargaHoraria": 80,
                  "escolaId": "%s"
                }
                """.formatted(turmaDisciplinaId, turmaId, UUID.randomUUID(), ESCOLA_ID)));
        catalogServer.enqueue(json("""
                {
                  "id": "%s",
                  "codigo": "T-A",
                  "nome": "Turma A",
                  "capacidade": 30,
                  "periodoLetivoId": "%s",
                  "serieId": "%s",
                  "serieNome": "1 ano",
                  "turnoId": "%s",
                  "turnoCodigo": "MANHA",
                  "ativo": true,
                  "escolaId": "%s"
                }
                """.formatted(turmaId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ESCOLA_ID)));

        mockMvc.perform(post("/internal/v1/professores/{id}/turmas-disciplinas", professorId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "turmaDisciplinaId": "%s",
                                  "dataInicio": "2026-02-01",
                                  "ativo": true
                                }
                                """.formatted(turmaDisciplinaId))
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-allocate")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma A"))
                .andExpect(jsonPath("$.disciplinaNome").value("Matematica"));

        mockMvc.perform(get("/internal/v1/professores/{id}/turmas-disciplinas", professorId)
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-aloc-list")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].turmaNome").value("Turma A"));

        mockMvc.perform(get("/internal/v1/turmas/{turmaId}/professores", turmaId)
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-turma")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorNome").value("Ana Professor"));

        UUID elegivelFuncionarioId = UUID.randomUUID();
        UUID elegivelPessoaId = UUID.randomUUID();
        peopleServer.enqueue(json("""
                [
                  {
                    "funcionarioId": "%s",
                    "pessoaId": "%s",
                    "escolaId": "%s",
                    "nomeCompleto": "Ana Professor",
                    "cargoDescricao": "Coordenadora",
                    "ativo": true
                  },
                  {
                    "funcionarioId": "%s",
                    "pessoaId": "%s",
                    "escolaId": "%s",
                    "nomeCompleto": "Carlos Elegivel",
                    "cargoDescricao": "Assistente",
                    "ativo": true
                  }
                ]
                """.formatted(funcionarioId, pessoaId, ESCOLA_ID, elegivelFuncionarioId, elegivelPessoaId, ESCOLA_ID)));
        peopleServer.enqueue(json("""
                {
                  "id": "%s",
                  "nomeCompleto": "Carlos Elegivel",
                  "escolaId": "%s",
                  "escolaNome": "Escola Central",
                  "ativo": true
                }
                """.formatted(elegivelPessoaId, ESCOLA_ID)));

        mockMvc.perform(get("/internal/v1/professores/funcionarios-elegiveis")
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-elegiveis")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].funcionarioId").value(elegivelFuncionarioId.toString()))
                .andExpect(jsonPath("$[0].escolaNome").value("Escola Central"))
                .andExpect(jsonPath("$[0].elegivelProfessor").value(true));

        RecordedRequest peopleCreateRequest = peopleServer.takeRequest();
        assertThat(peopleCreateRequest.getPath()).isEqualTo("/internal/v1/funcionarios/" + funcionarioId);
        assertThat(peopleCreateRequest.getHeader("X-Internal-Token")).isEqualTo("people-token");

        RecordedRequest catalogRequest = catalogServer.takeRequest();
        assertThat(catalogRequest.getPath()).isEqualTo("/internal/v1/turmas-disciplinas/" + turmaDisciplinaId);
    }

    @Test
    void deveResponderConflitoQuandoPessoaJaEstaVinculadaAProfessor() throws Exception {
        UUID funcionarioId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        peopleServer.enqueue(json("""
                {
                  "funcionarioId": "%s",
                  "pessoaId": "%s",
                  "escolaId": "%s",
                  "nomeCompleto": "Ana Professor",
                  "cargoDescricao": "Coordenadora",
                  "ativo": true
                }
                """.formatted(funcionarioId, pessoaId, ESCOLA_ID)));
        peopleServer.enqueue(json("""
                {
                  "id": "%s",
                  "nomeCompleto": "Ana Professor",
                  "escolaId": "%s",
                  "escolaNome": "Escola Central",
                  "ativo": true
                }
                """.formatted(pessoaId, ESCOLA_ID)));
        mockMvc.perform(post("/internal/v1/professores")
                        .contentType("application/json")
                        .content("""
                                {
                                  "funcionarioId": "%s",
                                  "registroProfissional": "RP-123",
                                  "formacao": "Licenciatura",
                                  "ativo": true
                                }
                                """.formatted(funcionarioId))
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-create-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated());

        peopleServer.enqueue(json("""
                {
                  "funcionarioId": "%s",
                  "pessoaId": "%s",
                  "escolaId": "%s",
                  "nomeCompleto": "Ana Professor",
                  "cargoDescricao": "Coordenadora",
                  "ativo": true
                }
                """.formatted(funcionarioId, pessoaId, ESCOLA_ID)));
        peopleServer.enqueue(json("""
                {
                  "id": "%s",
                  "nomeCompleto": "Ana Professor",
                  "escolaId": "%s",
                  "escolaNome": "Escola Central",
                  "ativo": true
                }
                """.formatted(pessoaId, ESCOLA_ID)));

        mockMvc.perform(post("/internal/v1/professores")
                        .contentType("application/json")
                        .content("""
                                {
                                  "funcionarioId": "%s",
                                  "registroProfissional": "RP-999",
                                  "formacao": "Licenciatura",
                                  "ativo": true
                                }
                                """.formatted(funcionarioId))
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-create-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_CONFLICT"));
    }

    @Test
    void deveResponderNotFoundQuandoProfessorNaoExisteLocalmente() throws Exception {
        mockMvc.perform(get("/internal/v1/professores/{id}", UUID.randomUUID())
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-missing")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void deveAtualizarSomenteOsCamposMutaveisDoProfessorNoEscopoDaEscola() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        var createdAt = java.time.LocalDateTime.of(2026, 1, 10, 8, 0);
        cadastroRepository.save(new CadastroJpaEntity(
                professorId,
                pessoaId,
                "Ana Professor",
                ESCOLA_ID,
                "Escola Central",
                "RP-ANTIGO",
                "Formacao antiga",
                true,
                createdAt,
                createdAt,
                UUID.randomUUID()));

        mockMvc.perform(put("/internal/v1/professores/{id}", professorId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "registroProfissional": "RP-ATUALIZADO",
                                  "formacao": "Licenciatura em Matematica",
                                  "ativo": false
                                }
                                """)
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-update")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.pessoaId").value(pessoaId.toString()))
                .andExpect(jsonPath("$.registroProfissional").value("RP-ATUALIZADO"))
                .andExpect(jsonPath("$.formacao").value("Licenciatura em Matematica"))
                .andExpect(jsonPath("$.ativo").value(false));

        CadastroJpaEntity persisted = cadastroRepository.findById(professorId).orElseThrow();
        assertThat(persisted.getPessoaId()).isEqualTo(pessoaId);
        assertThat(persisted.getEscolaId()).isEqualTo(ESCOLA_ID);
        assertThat(persisted.getCreatedAt()).isEqualTo(createdAt);
        assertThat(persisted.getUpdatedAt()).isAfter(createdAt);
    }

    @Test
    void naoDeveAtualizarProfessorDeOutraEscola() throws Exception {
        UUID professorId = UUID.randomUUID();
        cadastroRepository.save(new CadastroJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Professor Externo",
                UUID.randomUUID(),
                "Outra Escola",
                null,
                null,
                true,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                UUID.randomUUID()));

        mockMvc.perform(put("/internal/v1/professores/{id}", professorId)
                        .contentType("application/json")
                        .content("""
                                { "registroProfissional": "RP-ATUALIZADO", "ativo": true }
                                """)
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-update-other-school")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void naoDeveInativarProfessorComAlocacaoAcademicaAtiva() throws Exception {
        UUID professorId = UUID.randomUUID();
        cadastroRepository.save(new CadastroJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Ana Professor",
                ESCOLA_ID,
                "Escola Central",
                "RP-123",
                "Licenciatura",
                true,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                UUID.randomUUID()));
        alocacaoRepository.save(new br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity(
                UUID.randomUUID(),
                professorId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Turma A",
                UUID.randomUUID(),
                "Matematica",
                java.time.LocalDate.now(),
                null,
                true,
                java.time.LocalDateTime.now()));

        mockMvc.perform(put("/internal/v1/professores/{id}", professorId)
                        .contentType("application/json")
                        .content("""
                                { "registroProfissional": "RP-123", "formacao": "Licenciatura", "ativo": false }
                                """)
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-professor-inactivate")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_CONFLICT"));

        assertThat(cadastroRepository.findById(professorId).orElseThrow().getAtivo()).isTrue();
        assertThat(alocacaoRepository.existsByProfessorIdAndAtivoTrue(professorId)).isTrue();
    }

    @Test
    void deveAtualizarAlocacaoDoProfessorComVinculoAcademicoDaMesmaEscola() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();
        cadastroRepository.save(new CadastroJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Ana Professor",
                ESCOLA_ID,
                "Escola Central",
                "RP-123",
                "Licenciatura",
                true,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                UUID.randomUUID()));
        alocacaoRepository.save(new br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity(
                alocacaoId,
                professorId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Turma Antiga",
                UUID.randomUUID(),
                "Disciplina Antiga",
                java.time.LocalDate.of(2026, 2, 1),
                null,
                true,
                java.time.LocalDateTime.now()));

        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        catalogServer.enqueue(json("""
                {
                  "id": "%s",
                  "turmaId": "%s",
                  "disciplinaId": "%s",
                  "disciplinaNome": "Fisica",
                  "cargaHoraria": 60,
                  "escolaId": "%s"
                }
                """.formatted(turmaDisciplinaId, turmaId, disciplinaId, ESCOLA_ID)));
        catalogServer.enqueue(json("""
                {
                  "id": "%s",
                  "codigo": "T-B",
                  "nome": "Turma B",
                  "capacidade": 30,
                  "escolaId": "%s"
                }
                """.formatted(turmaId, ESCOLA_ID)));

        mockMvc.perform(put("/internal/v1/professores/{professorId}/turmas-disciplinas/{alocacaoId}", professorId, alocacaoId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "turmaDisciplinaId": "%s",
                                  "dataInicio": "2026-03-01",
                                  "dataFim": "2026-06-30",
                                  "ativo": false
                                }
                                """.formatted(turmaDisciplinaId))
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-alocacao-update")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.turmaDisciplinaId").value(turmaDisciplinaId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma B"))
                .andExpect(jsonPath("$.disciplinaNome").value("Fisica"))
                .andExpect(jsonPath("$.ativo").value(false));

        var persisted = alocacaoRepository.findById(alocacaoId).orElseThrow();
        assertThat(persisted.getProfessorId()).isEqualTo(professorId);
        assertThat(persisted.getTurmaDisciplinaId()).isEqualTo(turmaDisciplinaId);
        assertThat(persisted.getDataInicio()).isEqualTo(java.time.LocalDate.of(2026, 3, 1));
        assertThat(persisted.getDataFim()).isEqualTo(java.time.LocalDate.of(2026, 6, 30));
    }

    @Test
    void deveEncerrarAlocacaoSemApagarHistoricoELiberarNovoVinculoAtivo() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaDisciplinaId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        cadastroRepository.save(new CadastroJpaEntity(
                professorId,
                UUID.randomUUID(),
                "Ana Professor",
                ESCOLA_ID,
                "Escola Central",
                "RP-123",
                "Licenciatura",
                true,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                UUID.randomUUID()));
        alocacaoRepository.save(new br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity(
                alocacaoId,
                professorId,
                turmaDisciplinaId,
                turmaId,
                "Turma A",
                disciplinaId,
                "Matematica",
                java.time.LocalDate.of(2026, 2, 1),
                null,
                true,
                java.time.LocalDateTime.now()));

        mockMvc.perform(delete("/internal/v1/professores/{professorId}/turmas-disciplinas/{alocacaoId}", professorId, alocacaoId)
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-alocacao-close")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isNoContent());

        var encerrada = alocacaoRepository.findById(alocacaoId).orElseThrow();
        assertThat(encerrada.isAtivo()).isFalse();
        assertThat(encerrada.getDataFim()).isEqualTo(java.time.LocalDate.now());

        catalogServer.enqueue(json("""
                {
                  "id": "%s",
                  "turmaId": "%s",
                  "disciplinaId": "%s",
                  "disciplinaNome": "Matematica",
                  "cargaHoraria": 80,
                  "escolaId": "%s"
                }
                """.formatted(turmaDisciplinaId, turmaId, disciplinaId, ESCOLA_ID)));
        catalogServer.enqueue(json("""
                { "id": "%s", "codigo": "T-A", "nome": "Turma A", "escolaId": "%s" }
                """.formatted(turmaId, ESCOLA_ID)));

        mockMvc.perform(post("/internal/v1/professores/{id}/turmas-disciplinas", professorId)
                        .contentType("application/json")
                        .content("""
                                { "turmaDisciplinaId": "%s", "ativo": true }
                                """.formatted(turmaDisciplinaId))
                        .header("X-Internal-Token", "professor-token")
                        .header("X-Correlation-Id", "corr-alocacao-recreate")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativo").value(true));

        assertThat(alocacaoRepository.findAllByProfessorIdOrderByCreatedAtAsc(professorId)).hasSize(2);
        assertThat(alocacaoRepository.existsByProfessorIdAndTurmaDisciplinaIdAndAtivoTrue(
                professorId, turmaDisciplinaId)).isTrue();
    }

    private MockResponse json(String body) {
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private void drainRequests(MockWebServer server) throws InterruptedException {
        while (server.takeRequest(1, java.util.concurrent.TimeUnit.MILLISECONDS) != null) {
            // Each test owns its downstream request assertions.
        }
    }
}
