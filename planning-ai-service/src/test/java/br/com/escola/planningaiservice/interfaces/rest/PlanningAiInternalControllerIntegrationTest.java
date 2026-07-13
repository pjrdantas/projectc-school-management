package br.com.escola.planningaiservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiGeneratedContentJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiInteractionJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PedagogicalContentLibraryJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiContentVersionJpaRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class PlanningAiInternalControllerIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanningAiInteractionJpaRepository interactionRepository;

    @Autowired
    private PlanningAiGeneratedContentJpaRepository contentRepository;

    @Autowired
    private PlanningAiContentVersionJpaRepository versionRepository;

    @Autowired
    private PedagogicalContentLibraryJpaRepository libraryRepository;

    @BeforeEach
    void limparPersistenciaLocal() {
        libraryRepository.deleteAll();
        versionRepository.deleteAll();
        contentRepository.deleteAll();
        interactionRepository.deleteAll();
    }

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
        registry.add("planning-ai.internal-api.token", () -> "planning-token");
        registry.add("planning-ai.monolith.base-url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void deveListarBibliotecaNoContratoInterno() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        UUID conteudoId = UUID.randomUUID();
        UUID bibliotecaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        var content = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity();
        content.setId(conteudoId);
        content.setEscolaId(escolaId);
        content.setPlanejamentoBimestralId(UUID.randomUUID());
        content.setTitulo("Lista");
        content.setConteudo("Conteudo gerado");
        content.setVersao(1);
        content.setHashConteudo("abc123");
        content.setAprovadoPeloProfessor(false);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("APROVADO");
        content.setTipoConteudo("ATIVIDADE");
        content.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T10:15:30"));
        content.setUpdatedAt(java.time.LocalDateTime.parse("2026-07-13T10:15:30"));
        contentRepository.save(content);

        var library = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PedagogicalContentLibraryJpaEntity();
        library.setId(bibliotecaId);
        library.setEscolaId(escolaId);
        library.setConteudoOrigem(content);
        library.setProfessorId(professorId);
        library.setDisciplinaId(disciplinaId);
        library.setTipoConteudo("ATIVIDADE");
        library.setTitulo("Lista");
        library.setTema("Fracoes");
        library.setConteudo("Conteudo gerado");
        library.setOrigem("PLANEJAMENTO_IA");
        library.setReutilizavel(true);
        library.setAtivo(true);
        library.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T10:15:30"));
        library.setUpdatedAt(java.time.LocalDateTime.parse("2026-07-13T10:15:30"));
        libraryRepository.save(library);

        mockMvc.perform(get("/internal/v1/biblioteca-conteudos-pedagogicos")
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token")
                        .param("professorId", professorId.toString())
                        .param("disciplinaId", disciplinaId.toString())
                        .param("tipoConteudo", "ATIVIDADE")
                        .param("tema", "Fracoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bibliotecaId.toString()))
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].tipoConteudo").value("ATIVIDADE"))
                .andExpect(jsonPath("$[0].tipoConteudoDescricao").value("Atividade"));

        assertThat(mockWebServer.takeRequest(250, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void deveUsarFallbackDoMonolitoQuandoNaoHouverBibliotecaLocal() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        UUID bibliotecaId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "escolaId":"%s",
                            "escolaNome":"Escola Central",
                            "professorId":"%s",
                            "professorNome":"Professor Um",
                            "disciplinaId":"%s",
                            "disciplinaNome":"Matematica",
                            "tipoConteudo":"ATIVIDADE",
                            "tipoConteudoDescricao":"Atividade",
                            "titulo":"Lista",
                            "tema":"Fracoes",
                            "conteudo":"Conteudo gerado",
                            "origem":"PLANEJAMENTO_IA",
                            "reutilizavel":true,
                            "ativo":true,
                            "createdAt":"2026-07-13T10:15:30",
                            "updatedAt":"2026-07-13T10:15:30"
                          }
                        ]
                        """.formatted(bibliotecaId, UUID.randomUUID(), professorId, disciplinaId)));

        mockMvc.perform(get("/internal/v1/biblioteca-conteudos-pedagogicos")
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-1b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token")
                        .param("professorId", professorId.toString())
                        .param("disciplinaId", disciplinaId.toString())
                        .param("tipoConteudo", "ATIVIDADE")
                        .param("tema", "Fracoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bibliotecaId.toString()))
                .andExpect(jsonPath("$[0].professorNome").value("Professor Um"))
                .andExpect(jsonPath("$[0].tipoConteudo").value("ATIVIDADE"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo(
                "/api/biblioteca-conteudos-pedagogicos?professorId=" + professorId
                        + "&disciplinaId=" + disciplinaId
                        + "&tipoConteudo=ATIVIDADE&tema=Fracoes");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
    }

    @Test
    void deveGerarConteudoNoContratoInterno() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID conteudoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "planejamentoBimestralId":"%s",
                          "interacaoId":"%s",
                          "escolaId":"%s",
                          "escolaNome":"Escola Central",
                          "titulo":"Sugestao - Fracoes",
                          "conteudo":"Conteudo gerado",
                          "versao":1,
                          "hashConteudo":"abc123",
                          "aprovadoPeloProfessor":false,
                          "reutilizavel":true,
                          "ativo":true,
                          "status":"GERADO",
                          "statusDescricao":"Gerado",
                          "tipoConteudo":"ATIVIDADE",
                          "tipoConteudoDescricao":"Atividade",
                          "createdAt":"2026-07-13T11:30:00",
                          "updatedAt":"2026-07-13T11:30:00"
                        }
                        """.formatted(conteudoId, planejamentoId, interacaoId, UUID.randomUUID())));

        mockMvc.perform(post("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-12")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "promptProfessor":"Monte uma atividade sobre fracoes",
                                  "tipoConteudo":"ATIVIDADE",
                                  "titulo":"Sugestao - Fracoes",
                                  "reutilizavel":true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(conteudoId.toString()))
                .andExpect(jsonPath("$.planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$.tipoConteudo").value("ATIVIDADE"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/planejamentos-bimestrais/" + planejamentoId + "/ia/conteudos");
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
        assertThat(interactionRepository.findById(interacaoId))
                .isPresent()
                .get()
                .satisfies(interaction -> {
                    assertThat(interaction.getEscolaId()).isEqualTo(escolaId);
                    assertThat(interaction.getPlanejamentoBimestralId()).isEqualTo(planejamentoId);
                    assertThat(interaction.getUsuarioId()).isEqualTo(usuarioId);
                    assertThat(interaction.getPromptProfessor()).isEqualTo("Monte uma atividade sobre fracoes");
                    assertThat(interaction.getRespostaIa()).isEqualTo("Conteudo gerado");
                });
        assertThat(contentRepository.findById(conteudoId))
                .isPresent()
                .get()
                .satisfies(content -> {
                    assertThat(content.getEscolaId()).isEqualTo(escolaId);
                    assertThat(content.getPlanejamentoBimestralId()).isEqualTo(planejamentoId);
                    assertThat(content.getInteracao()).isNotNull();
                    assertThat(content.getInteracao().getId()).isEqualTo(interacaoId);
                    assertThat(content.getStatus()).isEqualTo("GERADO");
                    assertThat(content.getTipoConteudo()).isEqualTo("ATIVIDADE");
                });
    }

    @Test
    void devePropagarNotFoundQuandoPlanejamentoNaoExisteNaGeracao() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID conteudoIdInexistente = UUID.randomUUID();
        UUID interacaoIdInexistente = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Planejamento bimestral nao encontrado"
                        }
                        """));

        mockMvc.perform(post("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-13")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "promptProfessor":"Monte uma atividade sobre fracoes",
                                  "tipoConteudo":"ATIVIDADE"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/planejamentos-bimestrais/" + planejamentoId + "/ia/conteudos");
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(interactionRepository.findById(interacaoIdInexistente)).isNotPresent();
        assertThat(contentRepository.findById(conteudoIdInexistente)).isNotPresent();
        assertThat(interactionRepository.count()).isZero();
        assertThat(contentRepository.count()).isZero();
    }

    @Test
    void devePropagarNotFoundQuandoTipoConteudoNaoExiste() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Tipo de conteudo nao encontrado"
                        }
                        """));

        mockMvc.perform(get("/internal/v1/biblioteca-conteudos-pedagogicos")
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token")
                        .param("tipoConteudo", "INVALIDO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/biblioteca-conteudos-pedagogicos?tipoConteudo=INVALIDO");
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/biblioteca-conteudos-pedagogicos")
                        .header("X-Correlation-Id", "corr-planning-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveListarInteracoesNoContratoInterno() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        var interaction = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiInteractionJpaEntity();
        interaction.setId(interacaoId);
        interaction.setEscolaId(escolaId);
        interaction.setPlanejamentoBimestralId(planejamentoId);
        interaction.setUsuarioId(usuarioId);
        interaction.setPromptProfessor("Monte uma atividade sobre fracoes");
        interaction.setRespostaIa("Sugestao de atividade");
        interaction.setModeloIa("gpt-4.1");
        interaction.setTokensEntrada(120);
        interaction.setTokensSaida(340);
        interaction.setCustoEstimado(new java.math.BigDecimal("1.25"));
        interaction.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T11:00:00"));
        interactionRepository.save(interaction);

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/interacoes", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-4")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(interacaoId.toString()))
                .andExpect(jsonPath("$[0].planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$[0].modeloIA").value("gpt-4.1"));
        assertThat(mockWebServer.takeRequest(250, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void devePropagarNotFoundQuandoPlanejamentoNaoExisteNasInteracoes() throws Exception {
        UUID planejamentoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Planejamento bimestral nao encontrado"
                        }
                        """));

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/interacoes", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-5")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/planejamentos-bimestrais/" + planejamentoId + "/ia/interacoes");
    }

    @Test
    void deveUsarFallbackDoMonolitoQuandoNaoHouverInteracoesLocais() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "planejamentoBimestralId":"%s",
                            "escolaId":"%s",
                            "escolaNome":"Escola Central",
                            "promptProfessor":"Fallback",
                            "respostaIA":"Resposta do monolito",
                            "modeloIA":"gpt-4.1",
                            "tokensEntrada":100,
                            "tokensSaida":200,
                            "custoEstimado":1.00,
                            "createdAt":"2026-07-13T11:00:00"
                          }
                        ]
                        """.formatted(interacaoId, planejamentoId, UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/interacoes", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-4b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(interacaoId.toString()))
                .andExpect(jsonPath("$[0].respostaIA").value("Resposta do monolito"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/planejamentos-bimestrais/" + planejamentoId + "/ia/interacoes");
    }

    @Test
    void deveListarConteudosNoContratoInterno() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID conteudoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        var interaction = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiInteractionJpaEntity();
        interaction.setId(interacaoId);
        interaction.setEscolaId(escolaId);
        interaction.setPlanejamentoBimestralId(planejamentoId);
        interaction.setPromptProfessor("Monte uma atividade sobre fracoes");
        interaction.setRespostaIa("Sugestao de atividade");
        interaction.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T11:00:00"));
        interactionRepository.save(interaction);

        var content = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity();
        content.setId(conteudoId);
        content.setEscolaId(escolaId);
        content.setPlanejamentoBimestralId(planejamentoId);
        content.setInteracao(interaction);
        content.setTitulo("Lista de fracoes");
        content.setConteudo("Conteudo gerado");
        content.setVersao(1);
        content.setHashConteudo("abc123");
        content.setAprovadoPeloProfessor(false);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("GERADO");
        content.setTipoConteudo("ATIVIDADE");
        content.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T11:10:00"));
        content.setUpdatedAt(java.time.LocalDateTime.parse("2026-07-13T11:10:00"));
        contentRepository.save(content);

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-6")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(conteudoId.toString()))
                .andExpect(jsonPath("$[0].planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$[0].tipoConteudo").value("ATIVIDADE"))
                .andExpect(jsonPath("$[0].statusDescricao").value("Gerado"))
                .andExpect(jsonPath("$[0].tipoConteudoDescricao").value("Atividade"));

        assertThat(mockWebServer.takeRequest(250, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void devePropagarNotFoundQuandoPlanejamentoNaoExisteNosConteudos() throws Exception {
        UUID planejamentoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Planejamento bimestral nao encontrado"
                        }
                        """));

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-7")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/planejamentos-bimestrais/" + planejamentoId + "/ia/conteudos");
    }

    @Test
    void deveUsarFallbackDoMonolitoQuandoNaoHouverConteudosLocais() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID conteudoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "planejamentoBimestralId":"%s",
                            "interacaoId":"%s",
                            "escolaId":"%s",
                            "escolaNome":"Escola Central",
                            "titulo":"Fallback",
                            "conteudo":"Conteudo do monolito",
                            "versao":1,
                            "hashConteudo":"abc123",
                            "aprovadoPeloProfessor":false,
                            "reutilizavel":true,
                            "ativo":true,
                            "status":"GERADO",
                            "statusDescricao":"Gerado",
                            "tipoConteudo":"ATIVIDADE",
                            "tipoConteudoDescricao":"Atividade",
                            "createdAt":"2026-07-13T11:10:00",
                            "updatedAt":"2026-07-13T11:10:00"
                          }
                        ]
                        """.formatted(conteudoId, planejamentoId, interacaoId, UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-6b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(conteudoId.toString()))
                .andExpect(jsonPath("$[0].conteudo").value("Conteudo do monolito"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/planejamentos-bimestrais/" + planejamentoId + "/ia/conteudos");
    }

    @Test
    void deveBuscarConteudoNoContratoInterno() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID planejamentoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        var interaction = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiInteractionJpaEntity();
        interaction.setId(interacaoId);
        interaction.setEscolaId(escolaId);
        interaction.setPlanejamentoBimestralId(planejamentoId);
        interaction.setPromptProfessor("Monte uma atividade sobre fracoes");
        interaction.setRespostaIa("Sugestao de atividade");
        interaction.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T11:00:00"));
        interactionRepository.save(interaction);

        var content = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity();
        content.setId(conteudoId);
        content.setEscolaId(escolaId);
        content.setPlanejamentoBimestralId(planejamentoId);
        content.setInteracao(interaction);
        content.setTitulo("Lista de fracoes");
        content.setConteudo("Conteudo gerado");
        content.setVersao(1);
        content.setHashConteudo("abc123");
        content.setAprovadoPeloProfessor(false);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("GERADO");
        content.setTipoConteudo("ATIVIDADE");
        content.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T11:10:00"));
        content.setUpdatedAt(java.time.LocalDateTime.parse("2026-07-13T11:10:00"));
        contentRepository.save(content);

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-8")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conteudoId.toString()))
                .andExpect(jsonPath("$.planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$.tipoConteudo").value("ATIVIDADE"))
                .andExpect(jsonPath("$.statusDescricao").value("Gerado"))
                .andExpect(jsonPath("$.tipoConteudoDescricao").value("Atividade"));

        assertThat(mockWebServer.takeRequest(250, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void deveUsarFallbackDoMonolitoQuandoNaoHouverConteudoLocalPorId() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID planejamentoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "planejamentoBimestralId":"%s",
                          "interacaoId":"%s",
                          "escolaId":"%s",
                          "escolaNome":"Escola Central",
                          "titulo":"Fallback",
                          "conteudo":"Conteudo do monolito",
                          "versao":1,
                          "hashConteudo":"abc123",
                          "aprovadoPeloProfessor":false,
                          "reutilizavel":true,
                          "ativo":true,
                          "status":"GERADO",
                          "statusDescricao":"Gerado",
                          "tipoConteudo":"ATIVIDADE",
                          "tipoConteudoDescricao":"Atividade",
                          "createdAt":"2026-07-13T11:10:00",
                          "updatedAt":"2026-07-13T11:10:00"
                        }
                        """.formatted(conteudoId, planejamentoId, interacaoId, UUID.randomUUID())));

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-8b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conteudoId.toString()))
                .andExpect(jsonPath("$.conteudo").value("Conteudo do monolito"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId);
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
    }

    @Test
    void deveCriarVersaoNoContratoInterno() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID versaoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        var content = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity();
        content.setId(conteudoId);
        content.setEscolaId(escolaId);
        content.setPlanejamentoBimestralId(UUID.randomUUID());
        content.setTitulo("Lista de fracoes");
        content.setConteudo("Conteudo original");
        content.setVersao(1);
        content.setHashConteudo("abc123");
        content.setAprovadoPeloProfessor(false);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("GERADO");
        content.setTipoConteudo("ATIVIDADE");
        content.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T11:10:00"));
        content.setUpdatedAt(java.time.LocalDateTime.parse("2026-07-13T11:10:00"));
        contentRepository.save(content);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "conteudoGeradoId":"%s",
                          "numeroVersao":2,
                          "conteudo":"Conteudo revisado",
                          "motivoAlteracao":"Ajuste do professor",
                          "createdAt":"2026-07-13T12:10:00"
                        }
                        """.formatted(versaoId, conteudoId)));

        String requestBody = """
                {
                  "conteudo":"Conteudo revisado",
                  "motivoAlteracao":"Ajuste do professor"
                }
                """;

        mockMvc.perform(post("/internal/v1/ia/conteudos/{conteudoId}/versoes", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-14")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(versaoId.toString()))
                .andExpect(jsonPath("$.conteudoGeradoId").value(conteudoId.toString()))
                .andExpect(jsonPath("$.numeroVersao").value(2));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId + "/versoes");
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
        assertThat(recorded.getBody().readUtf8())
                .isEqualTo("{\"conteudo\":\"Conteudo revisado\",\"motivoAlteracao\":\"Ajuste do professor\"}");
        assertThat(versionRepository.findById(versaoId))
                .isPresent()
                .get()
                .satisfies(version -> {
                    assertThat(version.getEscolaId()).isEqualTo(escolaId);
                    assertThat(version.getAlteradoPor()).isEqualTo(usuarioId);
                    assertThat(version.getConteudoGerado().getId()).isEqualTo(conteudoId);
                    assertThat(version.getNumeroVersao()).isEqualTo(2);
                    assertThat(version.getConteudo()).isEqualTo("Conteudo revisado");
                });
        assertThat(contentRepository.findById(conteudoId))
                .isPresent()
                .get()
                .satisfies(savedContent -> {
                    assertThat(savedContent.getVersao()).isEqualTo(2);
                    assertThat(savedContent.getConteudo()).isEqualTo("Conteudo revisado");
                });
    }

    @Test
    void devePropagarNotFoundQuandoConteudoNaoExiste() throws Exception {
        UUID conteudoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Conteudo IA nao encontrado"
                        }
                        """));

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-9")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId);
    }

    @Test
    void devePropagarNotFoundQuandoConteudoNaoExisteNaCriacaoDeVersao() throws Exception {
        UUID conteudoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Conteudo IA nao encontrado"
                        }
                        """));

        mockMvc.perform(post("/internal/v1/ia/conteudos/{conteudoId}/versoes", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-15")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "conteudo":"Conteudo revisado",
                                  "motivoAlteracao":"Ajuste do professor"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId + "/versoes");
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(versionRepository.count()).isZero();
    }

    @Test
    void deveAprovarVersaoNoContratoInterno() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID planejamentoId = UUID.randomUUID();
        UUID interacaoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        var content = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity();
        content.setId(conteudoId);
        content.setEscolaId(escolaId);
        content.setPlanejamentoBimestralId(planejamentoId);
        content.setTitulo("Lista de fracoes");
        content.setConteudo("Conteudo revisado parcialmente");
        content.setVersao(1);
        content.setHashConteudo("abc123");
        content.setAprovadoPeloProfessor(false);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("GERADO");
        content.setTipoConteudo("ATIVIDADE");
        content.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T12:20:00"));
        content.setUpdatedAt(java.time.LocalDateTime.parse("2026-07-13T12:20:00"));
        contentRepository.save(content);

        var version = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiContentVersionJpaEntity();
        version.setId(UUID.randomUUID());
        version.setEscolaId(escolaId);
        version.setConteudoGerado(content);
        version.setNumeroVersao(2);
        version.setConteudo("Conteudo revisado parcialmente");
        version.setMotivoAlteracao("Ajuste do professor");
        version.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T12:10:00"));
        versionRepository.save(version);

        String requestBody = """
                {
                  "numeroVersao": 2,
                  "publicarBiblioteca": false
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "planejamentoBimestralId":"%s",
                          "interacaoId":"%s",
                          "escolaId":"%s",
                          "escolaNome":"Escola Central",
                          "titulo":"Lista de fracoes",
                          "conteudo":"Conteudo revisado",
                          "versao":2,
                          "hashConteudo":"abc123",
                          "aprovadoPeloProfessor":true,
                          "reutilizavel":true,
                          "ativo":true,
                          "status":"APROVADO",
                          "statusDescricao":"Aprovado",
                          "tipoConteudo":"ATIVIDADE",
                          "tipoConteudoDescricao":"Atividade",
                          "createdAt":"2026-07-13T12:20:00",
                          "updatedAt":"2026-07-13T12:25:00"
                        }
                        """.formatted(conteudoId, planejamentoId, interacaoId, UUID.randomUUID())));

        mockMvc.perform(patch("/internal/v1/ia/conteudos/{conteudoId}/aprovar-versao", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-16")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conteudoId.toString()))
                .andExpect(jsonPath("$.versao").value(2))
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.aprovadoPeloProfessor").value(true));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId + "/aprovar-versao");
        assertThat(recorded.getMethod()).isEqualTo("PATCH");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
        assertThat(recorded.getBody().readUtf8())
                .isEqualTo("{\"numeroVersao\":2,\"publicarBiblioteca\":false}");
        assertThat(contentRepository.findById(conteudoId))
                .isPresent()
                .get()
                .satisfies(savedContent -> {
                    assertThat(savedContent.getVersao()).isEqualTo(2);
                    assertThat(savedContent.getConteudo()).isEqualTo("Conteudo revisado");
                    assertThat(savedContent.isAprovadoPeloProfessor()).isTrue();
                    assertThat(savedContent.getStatus()).isEqualTo("APROVADO");
                });
        assertThat(versionRepository.findById(version.getId()))
                .isPresent()
                .get()
                .satisfies(savedVersion -> {
                    assertThat(savedVersion.getAlteradoPor()).isEqualTo(usuarioId);
                    assertThat(savedVersion.getConteudo()).isEqualTo("Conteudo revisado");
                });
    }

    @Test
    void devePropagarNotFoundQuandoConteudoNaoExisteNaAprovacaoDeVersao() throws Exception {
        UUID conteudoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Conteudo IA nao encontrado"
                        }
                        """));

        mockMvc.perform(patch("/internal/v1/ia/conteudos/{conteudoId}/aprovar-versao", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-17")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroVersao": 2,
                                  "publicarBiblioteca": false
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId + "/aprovar-versao");
        assertThat(recorded.getMethod()).isEqualTo("PATCH");
        assertThat(contentRepository.count()).isZero();
    }

    @Test
    void devePublicarBibliotecaNoContratoInterno() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID bibliotecaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        var content = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity();
        content.setId(conteudoId);
        content.setEscolaId(escolaId);
        content.setPlanejamentoBimestralId(UUID.randomUUID());
        content.setTitulo("Lista de fracoes");
        content.setConteudo("Conteudo revisado");
        content.setVersao(2);
        content.setHashConteudo("abc123");
        content.setAprovadoPeloProfessor(true);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("APROVADO");
        content.setTipoConteudo("ATIVIDADE");
        content.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T12:00:00"));
        content.setUpdatedAt(java.time.LocalDateTime.parse("2026-07-13T12:00:00"));
        contentRepository.save(content);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id":"%s",
                          "escolaId":"%s",
                          "escolaNome":"Escola Central",
                          "professorId":"%s",
                          "professorNome":"Professor Um",
                          "disciplinaId":"%s",
                          "disciplinaNome":"Matematica",
                          "tipoConteudo":"ATIVIDADE",
                          "tipoConteudoDescricao":"Atividade",
                          "titulo":"Lista de fracoes",
                          "tema":"Fracoes",
                          "conteudo":"Conteudo revisado",
                          "origem":"PLANEJAMENTO_IA",
                          "reutilizavel":true,
                          "ativo":true,
                          "createdAt":"2026-07-13T12:30:00",
                          "updatedAt":"2026-07-13T12:30:00"
                        }
                        """.formatted(bibliotecaId, UUID.randomUUID(), professorId, disciplinaId)));

        mockMvc.perform(post("/internal/v1/ia/conteudos/{conteudoId}/publicar-biblioteca", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-18")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bibliotecaId.toString()))
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.origem").value("PLANEJAMENTO_IA"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId + "/publicar-biblioteca");
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
        assertThat(libraryRepository.findById(bibliotecaId))
                .isPresent()
                .get()
                .satisfies(library -> {
                    assertThat(library.getEscolaId()).isEqualTo(escolaId);
                    assertThat(library.getProfessorId()).isEqualTo(professorId);
                    assertThat(library.getDisciplinaId()).isEqualTo(disciplinaId);
                    assertThat(library.getConteudoOrigem()).isNotNull();
                    assertThat(library.getConteudoOrigem().getId()).isEqualTo(conteudoId);
                    assertThat(library.getOrigem()).isEqualTo("PLANEJAMENTO_IA");
                });
    }

    @Test
    void devePropagarNotFoundQuandoConteudoNaoExisteNaPublicacaoBiblioteca() throws Exception {
        UUID conteudoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Conteudo IA nao encontrado"
                        }
                        """));

        mockMvc.perform(post("/internal/v1/ia/conteudos/{conteudoId}/publicar-biblioteca", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-19")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId + "/publicar-biblioteca");
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(libraryRepository.count()).isZero();
    }

    @Test
    void deveListarVersoesNoContratoInterno() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID versaoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        var content = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity();
        content.setId(conteudoId);
        content.setEscolaId(escolaId);
        content.setPlanejamentoBimestralId(UUID.randomUUID());
        content.setTitulo("Lista de fracoes");
        content.setConteudo("Conteudo gerado");
        content.setVersao(1);
        content.setHashConteudo("abc123");
        content.setAprovadoPeloProfessor(false);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("GERADO");
        content.setTipoConteudo("ATIVIDADE");
        content.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T11:10:00"));
        content.setUpdatedAt(java.time.LocalDateTime.parse("2026-07-13T11:10:00"));
        contentRepository.save(content);

        var version = new br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiContentVersionJpaEntity();
        version.setId(versaoId);
        version.setEscolaId(escolaId);
        version.setConteudoGerado(content);
        version.setNumeroVersao(2);
        version.setConteudo("Conteudo revisado");
        version.setMotivoAlteracao("Ajuste do professor");
        version.setCreatedAt(java.time.LocalDateTime.parse("2026-07-13T11:20:00"));
        versionRepository.save(version);

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}/versoes", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-10")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(versaoId.toString()))
                .andExpect(jsonPath("$[0].conteudoGeradoId").value(conteudoId.toString()))
                .andExpect(jsonPath("$[0].numeroVersao").value(2));

        assertThat(mockWebServer.takeRequest(250, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void devePropagarNotFoundQuandoConteudoNaoExisteNasVersoes() throws Exception {
        UUID conteudoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error":"RESOURCE_NOT_FOUND",
                          "message":"Conteudo IA nao encontrado"
                        }
                        """));

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}/versoes", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-11")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId + "/versoes");
    }

    @Test
    void deveUsarFallbackDoMonolitoQuandoNaoHouverVersoesLocais() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID versaoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        [
                          {
                            "id":"%s",
                            "conteudoGeradoId":"%s",
                            "numeroVersao":2,
                            "conteudo":"Conteudo revisado",
                            "motivoAlteracao":"Ajuste do professor",
                            "createdAt":"2026-07-13T11:20:00"
                          }
                        ]
                        """.formatted(versaoId, conteudoId)));

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}/versoes", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-10b")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(versaoId.toString()))
                .andExpect(jsonPath("$[0].conteudoGeradoId").value(conteudoId.toString()))
                .andExpect(jsonPath("$[0].numeroVersao").value(2));

        RecordedRequest recorded = aguardarRequisicao();
        assertThat(recorded.getPath()).isEqualTo("/api/ia/conteudos/" + conteudoId + "/versoes");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer planning-user-token");
    }

    private RecordedRequest aguardarRequisicao() throws InterruptedException {
        RecordedRequest recorded = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        return recorded;
    }
}
