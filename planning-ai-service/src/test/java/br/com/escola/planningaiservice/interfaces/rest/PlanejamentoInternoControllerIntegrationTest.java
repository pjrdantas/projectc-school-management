package br.com.escola.planningaiservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.BibliotecaConteudoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoGeradoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoVersaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.InteracaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.BibliotecaConteudoJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoGeradoJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoVersaoJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.InteracaoJpaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class PlanejamentoInternoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InteracaoJpaRepository interactionRepository;

    @Autowired
    private ConteudoGeradoJpaRepository contentRepository;

    @Autowired
    private ConteudoVersaoJpaRepository versionRepository;

    @Autowired
    private BibliotecaConteudoJpaRepository libraryRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("planning-ai.internal-api.token", () -> "planning-token");
    }

    @BeforeEach
    void limparPersistenciaLocal() {
        libraryRepository.deleteAll();
        versionRepository.deleteAll();
        contentRepository.deleteAll();
        interactionRepository.deleteAll();
    }

    @Test
    void deveListarBibliotecaNoContratoInterno() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        UUID conteudoId = UUID.randomUUID();
        UUID bibliotecaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        ConteudoGeradoJpaEntity content = conteudo(conteudoId, escolaId, "Lista", "Conteudo gerado", "APROVADO");
        contentRepository.save(content);

        BibliotecaConteudoJpaEntity library = new BibliotecaConteudoJpaEntity();
        library.setId(bibliotecaId);
        library.setEscolaId(escolaId);
        library.setEscolaNome("Escola Central");
        library.setConteudoOrigem(content);
        library.setProfessorId(professorId);
        library.setProfessorNome("Professor Um");
        library.setDisciplinaId(disciplinaId);
        library.setDisciplinaNome("Matematica");
        library.setTipoConteudo("ATIVIDADE");
        library.setTitulo("Lista");
        library.setTema("Fracoes");
        library.setConteudo("Conteudo gerado");
        library.setOrigem("PLANEJAMENTO_IA");
        library.setReutilizavel(true);
        library.setAtivo(true);
        library.setCreatedAt(LocalDateTime.parse("2026-07-13T10:15:30"));
        library.setUpdatedAt(LocalDateTime.parse("2026-07-13T10:15:30"));
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
                .andExpect(jsonPath("$[0].escolaNome").value("Escola Central"))
                .andExpect(jsonPath("$[0].professorNome").value("Professor Um"))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Matematica"))
                .andExpect(jsonPath("$[0].tipoConteudoDescricao").value("Atividade"));
    }

    @Test
    void deveGerarConteudoNoContratoInterno() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

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
                .andExpect(jsonPath("$.planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$.titulo").value("Sugestao - Fracoes"))
                .andExpect(jsonPath("$.status").value("GERADO"))
                .andExpect(jsonPath("$.tipoConteudo").value("ATIVIDADE"))
                .andExpect(jsonPath("$.aprovadoPeloProfessor").value(false));

        assertThat(interactionRepository.findAll())
                .singleElement()
                .satisfies(interaction -> {
                    assertThat(interaction.getEscolaId()).isEqualTo(escolaId);
                    assertThat(interaction.getPlanejamentoBimestralId()).isEqualTo(planejamentoId);
                    assertThat(interaction.getUsuarioId()).isEqualTo(usuarioId);
                    assertThat(interaction.getPromptProfessor()).isEqualTo("Monte uma atividade sobre fracoes");
                    assertThat(interaction.getModeloIa()).isEqualTo("LOCAL_RULE_BASED");
                });
        assertThat(contentRepository.findAll())
                .singleElement()
                .satisfies(content -> {
                    assertThat(content.getEscolaId()).isEqualTo(escolaId);
                    assertThat(content.getPlanejamentoBimestralId()).isEqualTo(planejamentoId);
                    assertThat(content.getStatus()).isEqualTo("GERADO");
                    assertThat(content.getHashConteudo()).isNotBlank();
                });
    }

    @Test
    void deveListarInteracoesEConteudosLocalmente() throws Exception {
        UUID planejamentoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        InteracaoJpaEntity interaction = new InteracaoJpaEntity();
        interaction.setId(UUID.randomUUID());
        interaction.setEscolaId(escolaId);
        interaction.setEscolaNome("Escola Central");
        interaction.setPlanejamentoBimestralId(planejamentoId);
        interaction.setUsuarioId(UUID.randomUUID());
        interaction.setPromptProfessor("Prompt");
        interaction.setRespostaIa("Resposta");
        interaction.setModeloIa("LOCAL_RULE_BASED");
        interaction.setCreatedAt(LocalDateTime.parse("2026-07-13T11:00:00"));
        interactionRepository.save(interaction);

        ConteudoGeradoJpaEntity content = conteudo(UUID.randomUUID(), escolaId, "Lista", "Conteudo", "GERADO");
        content.setPlanejamentoBimestralId(planejamentoId);
        content.setInteracao(interaction);
        contentRepository.save(content);

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/interacoes", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(interaction.getId().toString()))
                .andExpect(jsonPath("$[0].promptProfessor").value("Prompt"));

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(content.getId().toString()))
                .andExpect(jsonPath("$[0].status").value("GERADO"));
    }

    @Test
    void deveBuscarConteudoLocalmente() throws Exception {
        UUID escolaId = UUID.randomUUID();
        ConteudoGeradoJpaEntity content = conteudo(UUID.randomUUID(), escolaId, "Lista", "Conteudo", "GERADO");
        contentRepository.save(content);

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}", content.getId())
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-5")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(content.getId().toString()))
                .andExpect(jsonPath("$.titulo").value("Lista"));
    }

    @Test
    void deveRetornarNotFoundQuandoConteudoNaoExiste() throws Exception {
        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}", UUID.randomUUID())
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-6")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void deveCriarVersaoEAprovarConteudoNoContratoInterno() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        ConteudoGeradoJpaEntity content = conteudo(conteudoId, escolaId, "Lista de fracoes", "Conteudo inicial", "GERADO");
        contentRepository.save(content);

        mockMvc.perform(post("/internal/v1/ia/conteudos/{conteudoId}/versoes", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-7")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "conteudo":"Conteudo revisado",
                                  "motivoAlteracao":"Ajuste do professor"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conteudoGeradoId").value(conteudoId.toString()))
                .andExpect(jsonPath("$.numeroVersao").value(2));

        mockMvc.perform(patch("/internal/v1/ia/conteudos/{conteudoId}/aprovar-versao", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-8")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroVersao": 2,
                                  "publicarBiblioteca": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conteudoId.toString()))
                .andExpect(jsonPath("$.versao").value(2))
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.aprovadoPeloProfessor").value(true));

        assertThat(contentRepository.findById(conteudoId))
                .isPresent()
                .get()
                .satisfies(saved -> {
                    assertThat(saved.getConteudo()).isEqualTo("Conteudo revisado");
                    assertThat(saved.getVersao()).isEqualTo(2);
                    assertThat(saved.isAprovadoPeloProfessor()).isTrue();
                    assertThat(saved.getStatus()).isEqualTo("APROVADO");
                });
    }

    @Test
    void devePublicarBibliotecaNoContratoInterno() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        ConteudoGeradoJpaEntity content = conteudo(conteudoId, escolaId, "Lista - Fracoes", "Conteudo revisado", "APROVADO");
        content.setAprovadoPeloProfessor(true);
        contentRepository.save(content);

        mockMvc.perform(post("/internal/v1/ia/conteudos/{conteudoId}/publicar-biblioteca", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-9")
                        .header("X-Usuario-Id", usuarioId)
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andExpect(jsonPath("$.professorId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.tipoConteudo").value("ATIVIDADE"))
                .andExpect(jsonPath("$.origem").value("PLANEJAMENTO_IA"));

        assertThat(libraryRepository.findAll())
                .singleElement()
                .satisfies(library -> {
                    assertThat(library.getConteudoOrigem()).isNotNull();
                    assertThat(library.getConteudoOrigem().getId()).isEqualTo(conteudoId);
                    assertThat(library.getProfessorId()).isEqualTo(usuarioId);
                });
    }

    @Test
    void deveListarVersoesLocalmenteEValidarNotFound() throws Exception {
        UUID conteudoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        ConteudoGeradoJpaEntity content = conteudo(conteudoId, escolaId, "Lista", "Conteudo", "GERADO");
        contentRepository.save(content);

        ConteudoVersaoJpaEntity version = new ConteudoVersaoJpaEntity();
        version.setId(UUID.randomUUID());
        version.setEscolaId(escolaId);
        version.setConteudoGerado(content);
        version.setNumeroVersao(2);
        version.setConteudo("Conteudo revisado");
        version.setMotivoAlteracao("Ajuste");
        version.setCreatedAt(LocalDateTime.parse("2026-07-13T11:20:00"));
        versionRepository.save(version);

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}/versoes", conteudoId)
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-10")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(version.getId().toString()))
                .andExpect(jsonPath("$[0].numeroVersao").value(2));

        mockMvc.perform(get("/internal/v1/ia/conteudos/{conteudoId}/versoes", UUID.randomUUID())
                        .header("X-Internal-Token", "planning-token")
                        .header("X-Correlation-Id", "corr-planning-11")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", escolaId)
                        .header("Authorization", "Bearer planning-user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    private ConteudoGeradoJpaEntity conteudo(UUID conteudoId, UUID escolaId, String titulo, String conteudo, String status) {
        ConteudoGeradoJpaEntity entity = new ConteudoGeradoJpaEntity();
        entity.setId(conteudoId);
        entity.setEscolaId(escolaId);
        entity.setEscolaNome("Escola Central");
        entity.setPlanejamentoBimestralId(UUID.randomUUID());
        entity.setTitulo(titulo);
        entity.setConteudo(conteudo);
        entity.setVersao(1);
        entity.setHashConteudo("abc123");
        entity.setAprovadoPeloProfessor(false);
        entity.setReutilizavel(true);
        entity.setAtivo(true);
        entity.setStatus(status);
        entity.setTipoConteudo("ATIVIDADE");
        entity.setCreatedAt(LocalDateTime.parse("2026-07-13T10:15:30"));
        entity.setUpdatedAt(LocalDateTime.parse("2026-07-13T10:15:30"));
        return entity;
    }
}
