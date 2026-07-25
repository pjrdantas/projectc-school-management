package br.com.escola.planningaiservice.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanejamentoBimestralJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanejamentoBimestralAulaJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanejamentoBimestralAvaliacaoJpaRepository;

@SpringBootTest(properties = "planning-ai.internal-api.token=planning-ai-token")
@AutoConfigureMockMvc
class PlanejamentoBimestralInternoControllerIntegrationTest {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanejamentoBimestralJpaRepository repository;

    @Autowired
    private PlanejamentoBimestralAulaJpaRepository aulaRepository;

    @Autowired
    private PlanejamentoBimestralAvaliacaoJpaRepository avaliacaoRepository;

    @BeforeEach
    void setUp() {
        aulaRepository.deleteAll();
        avaliacaoRepository.deleteAll();
        repository.deleteAll();
    }

    @Test
    void deveCriarListarEConsultarPlanejamentoNoEscopoDaEscola() throws Exception {
        UUID professorTurmaDisciplinaId = UUID.randomUUID();
        UUID periodoAvaliativoId = UUID.randomUUID();
        String response = mockMvc.perform(post("/internal/v1/planejamentos-bimestrais")
                        .contentType("application/json")
                        .content("""
                                {
                                  "professorTurmaDisciplinaId": "%s",
                                  "periodoAvaliativoId": "%s",
                                  "titulo": "Planejamento do primeiro bimestre",
                                  "temaPrincipal": "Numeros naturais",
                                  "descricaoInicial": "Sequencia de atividades",
                                  "reutilizavel": true,
                                  "criadoComAuxilioIa": true
                                }
                                """.formatted(professorTurmaDisciplinaId, periodoAvaliativoId))
                        .header("X-Internal-Token", "planning-ai-token")
                        .header("X-Correlation-Id", "corr-b9-create")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_ID.toString()))
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(professorTurmaDisciplinaId.toString()))
                .andReturn().getResponse().getContentAsString();
        String planejamentoId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais")
                        .queryParam("professorTurmaDisciplinaId", professorTurmaDisciplinaId.toString())
                        .queryParam("periodoAvaliativoId", periodoAvaliativoId.toString())
                        .headers(headers("corr-b9-list", ESCOLA_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(planejamentoId));

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{id}", planejamentoId)
                        .headers(headers("corr-b9-detail", ESCOLA_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Planejamento do primeiro bimestre"));
    }

    @Test
    void naoDeveExporPlanejamentoDeOutraEscola() throws Exception {
        UUID professorTurmaDisciplinaId = UUID.randomUUID();
        String response = mockMvc.perform(post("/internal/v1/planejamentos-bimestrais")
                        .contentType("application/json")
                        .content("""
                                {
                                  "professorTurmaDisciplinaId": "%s",
                                  "titulo": "Planejamento isolado",
                                  "temaPrincipal": "Leitura",
                                  "descricaoInicial": "Atividades"
                                }
                                """.formatted(professorTurmaDisciplinaId))
                        .headers(headers("corr-b9-create-other", ESCOLA_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String planejamentoId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/internal/v1/planejamentos-bimestrais/{id}", planejamentoId)
                        .headers(headers("corr-b9-other-school", UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void deveAtualizarEControlarTransicoesDeStatus() throws Exception {
        String response = mockMvc.perform(post("/internal/v1/planejamentos-bimestrais")
                        .contentType("application/json")
                        .content("""
                                {
                                  "professorTurmaDisciplinaId": "%s",
                                  "titulo": "Planejamento inicial",
                                  "temaPrincipal": "Geometria",
                                  "descricaoInicial": "Atividades iniciais"
                                }
                                """.formatted(UUID.randomUUID()))
                        .headers(headers("corr-b9-status-create", ESCOLA_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String planejamentoId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(put("/internal/v1/planejamentos-bimestrais/{id}", planejamentoId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "professorTurmaDisciplinaId": "%s",
                                  "titulo": "Planejamento revisado",
                                  "temaPrincipal": "Geometria plana",
                                  "descricaoInicial": "Atividades revisadas",
                                  "reutilizavel": true
                                }
                                """.formatted(UUID.randomUUID()))
                        .headers(headers("corr-b9-update", ESCOLA_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Planejamento revisado"))
                .andExpect(jsonPath("$.reutilizavel").value(true));

        mockMvc.perform(patch("/internal/v1/planejamentos-bimestrais/{id}/status", planejamentoId)
                        .contentType("application/json")
                        .content("{\"status\":\"EM_ANALISE\"}")
                        .headers(headers("corr-b9-analysis", ESCOLA_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));

        mockMvc.perform(patch("/internal/v1/planejamentos-bimestrais/{id}/status", planejamentoId)
                        .contentType("application/json")
                        .content("{\"status\":\"APROVADO\"}")
                        .headers(headers("corr-b9-approve", ESCOLA_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprovadoPeloProfessor").value(true));

        mockMvc.perform(patch("/internal/v1/planejamentos-bimestrais/{id}/status", planejamentoId)
                        .contentType("application/json")
                        .content("{\"status\":\"APROVADO\"}")
                        .headers(headers("corr-b9-approve-repeat", ESCOLA_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        mockMvc.perform(patch("/internal/v1/planejamentos-bimestrais/{id}/status", planejamentoId)
                        .contentType("application/json")
                        .content("{\"status\":\"RASCUNHO\"}")
                        .headers(headers("corr-b9-invalid-transition", ESCOLA_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_CONFLICT"));
    }

    @Test
    void deveAdicionarAulaEAvaliacaoPrevistasDeFormaIdempotente() throws Exception {
        String createResponse = mockMvc.perform(post("/internal/v1/planejamentos-bimestrais")
                        .contentType("application/json")
                        .content("""
                                {
                                  "professorTurmaDisciplinaId": "%s",
                                  "titulo": "Planejamento com subrecursos",
                                  "temaPrincipal": "Ciencias",
                                  "descricaoInicial": "Atividades"
                                }
                                """.formatted(UUID.randomUUID()))
                        .headers(headers("corr-b9-subresource-create", ESCOLA_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String planejamentoId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");
        String aulaBody = """
                {
                  "numeroAula": 1,
                  "temaAula": "Ecossistemas",
                  "objetivoAula": "Compreender relacoes"
                }
                """;
        String avaliacaoBody = """
                {
                  "titulo": "Avaliacao diagnostica",
                  "peso": 1.0,
                  "valorMaximo": 10.0,
                  "tipoAvaliacao": "PROVA"
                }
                """;

        mockMvc.perform(post("/internal/v1/planejamentos-bimestrais/{id}/aulas-previstas", planejamentoId)
                        .contentType("application/json").content(aulaBody)
                        .headers(headers("corr-b9-aula-1", ESCOLA_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroAula").value(1));
        mockMvc.perform(post("/internal/v1/planejamentos-bimestrais/{id}/aulas-previstas", planejamentoId)
                        .contentType("application/json").content(aulaBody)
                        .headers(headers("corr-b9-aula-repeat", ESCOLA_ID)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/internal/v1/planejamentos-bimestrais/{id}/aulas-previstas", planejamentoId)
                        .contentType("application/json")
                        .content("{\"numeroAula\":1,\"temaAula\":\"Outro tema\"}")
                        .headers(headers("corr-b9-aula-conflict", ESCOLA_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_CONFLICT"));

        mockMvc.perform(post("/internal/v1/planejamentos-bimestrais/{id}/avaliacoes-previstas", planejamentoId)
                        .contentType("application/json").content(avaliacaoBody)
                        .headers(headers("corr-b9-avaliacao-1", ESCOLA_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoAvaliacao").value("PROVA"));
        mockMvc.perform(post("/internal/v1/planejamentos-bimestrais/{id}/avaliacoes-previstas", planejamentoId)
                        .contentType("application/json").content(avaliacaoBody)
                        .headers(headers("corr-b9-avaliacao-repeat", ESCOLA_ID)))
                .andExpect(status().isCreated());

        org.assertj.core.api.Assertions.assertThat(aulaRepository.count()).isOne();
        org.assertj.core.api.Assertions.assertThat(avaliacaoRepository.count()).isOne();
    }

    private org.springframework.http.HttpHeaders headers(String correlationId, UUID escolaId) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Internal-Token", "planning-ai-token");
        headers.add("X-Correlation-Id", correlationId);
        headers.add("X-Usuario-Id", UUID.randomUUID().toString());
        headers.add("X-Escola-Id", escolaId.toString());
        return headers;
    }
}
