package br.com.escola.academiccatalog.adapter.in.web;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        @SuppressWarnings("null")
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
    void deveCadastrarEConsultarTurmaPorId() throws Exception {
        String periodoRequest = """
                {
                  "nome": "2026.2",
                  "dataInicio": "2026-08-01",
                  "dataFim": "2026-12-15"
                }
                """;

        @SuppressWarnings("null")
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
                  "periodoLetivoId": "%s"
                }
                """.formatted(periodoId);

        @SuppressWarnings("null")
		String turmaResponse = mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turmaRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID turmaId = UUID.fromString(objectMapper.readTree(turmaResponse).get("id").asText());

        mockMvc.perform(get("/api/turmas/{id}", turmaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(turmaId.toString()))
                .andExpect(jsonPath("$.codigo").value("TURMA-A"));
    }

    @SuppressWarnings("null")
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
                  "periodoLetivoId": "%s"
                }
                """.formatted(periodoId);

        mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turmaRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turmaRequest))
                .andExpect(status().isConflict());
    }
}
