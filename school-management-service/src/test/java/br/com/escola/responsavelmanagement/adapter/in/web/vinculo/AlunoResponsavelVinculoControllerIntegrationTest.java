package br.com.escola.responsavelmanagement.adapter.in.web.vinculo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AlunoResponsavelVinculoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("null")
	@Test
    @WithMockUser
    void deveVincularListarEDesvincularResponsavelDoAluno() throws Exception {
        String alunoId = criarAluno("Pedro Alves", "12121212121");
        String responsavelId = criarResponsavel("Ana Alves", "23232323232");

        String bodyVinculo = """
                {
                  "idResponsavel": "%s"
                }
                """.formatted(responsavelId);

        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis", alunoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyVinculo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/alunos/{idAluno}/responsaveis", alunoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responsavelId))
                .andExpect(jsonPath("$[0].nomeCompleto").value("Ana Alves"));

        mockMvc.perform(delete("/api/alunos/{idAluno}/responsaveis/{idResponsavel}", alunoId, responsavelId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/alunos/{idAluno}/responsaveis", alunoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @SuppressWarnings("null")
	@Test
    @WithMockUser
    void deveRetornarConflitoQuandoVinculoDuplicado() throws Exception {
        String alunoId = criarAluno("Paulo Maia", "34343434343");
        String responsavelId = criarResponsavel("Rita Maia", "45454545454");

        String bodyVinculo = """
                {
                  "idResponsavel": "%s"
                }
                """.formatted(responsavelId);

        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis", alunoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyVinculo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis", alunoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyVinculo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vínculo entre aluno e responsável já existe"));
    }

    private String criarAluno(String nome, String cpf) throws Exception {
        String body = """
                {
                  "nomeCompleto": "%s",
                  "cpf": "%s",
                  "email": "aluno@example.com",
                  "dataNascimento": "2011-09-15"
                }
                """.formatted(nome, cpf);

        @SuppressWarnings("null")
		String response = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String criarResponsavel(String nome, String cpf) throws Exception {
        String body = """
                {
                  "nomeCompleto": "%s",
                  "cpf": "%s",
                  "email": "responsavel@example.com",
                  "telefone": "11911112222"
                }
                """.formatted(nome, cpf);

        @SuppressWarnings("null")
		String response = mockMvc.perform(post("/api/responsaveis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }
}
