package br.com.escola.academiccatalog.adapter.in.web;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AcademicCatalogControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void deveCadastrarEConsultarPeriodoLetivoPorId() throws Exception {
        String requestBody = """
                {
                  "nome": "2026.1",
                  "dataInicio": "2026-02-01",
                  "dataFim": "2026-06-30"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/periodos-letivos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        UUID id = UUID.fromString(json.get("id").asText());

        mockMvc.perform(get("/api/periodos-letivos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("2026.1"));
    }

    @Test
    @WithMockUser
    void deveRetornarNotFoundQuandoPeriodoLetivoNaoExistir() throws Exception {
        mockMvc.perform(get("/api/periodos-letivos/{id}", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveCadastrarEConsultarSeriePorId() throws Exception {
        String requestBody = """
                {
                  "nome": "1º ano",
                  "ordem": 1,
                  "nivelEnsino": "Ensino Fundamental"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("1º ano"))
                .andExpect(jsonPath("$.ordem").value(1))
                .andExpect(jsonPath("$.nivelEnsino").value("ENSINO_FUNDAMENTAL"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        mockMvc.perform(get("/api/series/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("1º ano"));
    }

    @Test
    @WithMockUser
    void deveCadastrarEConsultarTurnoPorId() throws Exception {
        String requestBody = """
                {
                  "codigo": "vespertino especial",
                  "descricao": "Vespertino especial"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("VESPERTINO_ESPECIAL"))
                .andExpect(jsonPath("$.descricao").value("Vespertino especial"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        mockMvc.perform(get("/api/turnos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.codigo").value("VESPERTINO_ESPECIAL"));
    }

    @Test
    @WithMockUser
    void deveAtualizarTurnoEPersistirDescricao() throws Exception {
        UUID turnoId = criarTurno("turno apoio", "Turno de apoio");

        String updateRequest = """
                {
                  "codigo": "apoio pedagogico",
                  "descricao": "Apoio pedagogico"
                }
                """;

        mockMvc.perform(put("/api/turnos/{id}", turnoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(turnoId.toString()))
                .andExpect(jsonPath("$.codigo").value("APOIO_PEDAGOGICO"))
                .andExpect(jsonPath("$.descricao").value("Apoio pedagogico"));

        mockMvc.perform(get("/api/turnos/{id}", turnoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Apoio pedagogico"));
    }

    @Test
    @WithMockUser
    void deveRetornarNotFoundQuandoTurnoNaoExistir() throws Exception {
        mockMvc.perform(get("/api/turnos/{id}", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveCadastrarEConsultarTurmaPorId() throws Exception {
        String periodoRequest = """
                {
                  "nome": "2026.2",
                  "dataInicio": "2026-08-01",
                  "dataFim": "2026-12-15"
                }
                """;

        String periodoResponse = mockMvc.perform(post("/api/periodos-letivos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(periodoRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID periodoId = UUID.fromString(objectMapper.readTree(periodoResponse).get("id").asText());

        String turmaRequest = """
                {
                  "codigo": "TURMA-A",
                  "nome": "Turma A",
                  "capacidade": 30,
                  "periodoLetivoId": "%s",
                  "serieId": "%s",
                  "turno": "MANHA",
                  "status": "ATIVA"
                }
                """.formatted(periodoId, SERIE_PADRAO_ID);

        String turmaResponse = mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turmaRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoId.toString()))
                .andExpect(jsonPath("$.serieId").value(SERIE_PADRAO_ID.toString()))
                .andExpect(jsonPath("$.turno").value("MANHA"))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID turmaId = UUID.fromString(objectMapper.readTree(turmaResponse).get("id").asText());

        mockMvc.perform(get("/api/turmas/{id}", turmaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(turmaId.toString()))
                .andExpect(jsonPath("$.codigo").value("TURMA-A"));
    }

    @Test
    @WithMockUser
    void deveAtualizarTurmaEPersistirCapacidade() throws Exception {
        UUID periodoId = criarPeriodo("2028.1", "2028-02-01", "2028-06-30");
        UUID turmaId = criarTurma("TURMA-UPDATE-A", "Turma Update A", 30, periodoId);

        String updateRequest = """
                {
                  "codigo": "TURMA-UPDATE-A",
                  "nome": "Turma Update A",
                  "capacidade": 12,
                  "periodoLetivoId": "%s",
                  "serieId": "%s",
                  "turno": "TARDE",
                  "status": "ATIVA"
                }
                """.formatted(periodoId, SERIE_PADRAO_ID);

        mockMvc.perform(put("/api/turmas/{id}", turmaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(turmaId.toString()))
                .andExpect(jsonPath("$.capacidade").value(12))
                .andExpect(jsonPath("$.turno").value("TARDE"));

        mockMvc.perform(get("/api/turmas/{id}", turmaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidade").value(12));
    }

    @Test
    @WithMockUser
    void deveRetornarConflictQuandoTurmaDuplicadaNoMesmoPeriodo() throws Exception {
        String periodoRequest = """
                {
                  "nome": "2027.1",
                  "dataInicio": "2027-02-01",
                  "dataFim": "2027-06-30"
                }
                """;

        String periodoResponse = mockMvc.perform(post("/api/periodos-letivos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(periodoRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID periodoId = UUID.fromString(objectMapper.readTree(periodoResponse).get("id").asText());

        String turmaRequest = """
                {
                  "codigo": "TURMA-B",
                  "nome": "Turma B",
                  "capacidade": 35,
                  "periodoLetivoId": "%s",
                  "serieId": "%s"
                }
                """.formatted(periodoId, SERIE_PADRAO_ID);

        mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turmaRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turmaRequest))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void deveRetornarBadRequestQuandoCodigoDaTurmaExcederLimite() throws Exception {
        UUID periodoId = criarPeriodo("2029.1", "2029-02-01", "2029-06-30");

        String turmaRequest = """
                {
                  "codigo": "CODIGO-DE-TURMA-MUITO-LONGO",
                  "nome": "Turma com codigo invalido",
                  "capacidade": 30,
                  "periodoLetivoId": "%s",
                  "serieId": "%s",
                  "turno": "MANHA",
                  "status": "ATIVA"
                }
                """.formatted(periodoId, SERIE_PADRAO_ID);

        mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turmaRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos"));
    }

    private UUID criarPeriodo(String nome, String dataInicio, String dataFim) throws Exception {
        String requestBody = """
                {
                  "nome": "%s",
                  "dataInicio": "%s",
                  "dataFim": "%s"
                }
                """.formatted(nome, dataInicio, dataFim);

        String responseBody = mockMvc.perform(post("/api/periodos-letivos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarTurno(String codigo, String descricao) throws Exception {
        String requestBody = """
                {
                  "codigo": "%s",
                  "descricao": "%s"
                }
                """.formatted(codigo, descricao);

        String responseBody = mockMvc.perform(post("/api/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarTurma(String codigo, String nome, int capacidade, UUID periodoId) throws Exception {
        String requestBody = """
                {
                  "codigo": "%s",
                  "nome": "%s",
                  "capacidade": %d,
                  "periodoLetivoId": "%s",
                  "serieId": "%s"
                }
                """.formatted(codigo, nome, capacidade, periodoId, SERIE_PADRAO_ID);

        String responseBody = mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }
}
