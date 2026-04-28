package br.com.escola.responsavelmanagement.adapter.in.web;

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
class ResponsavelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void deveCriarEBuscarResponsavelPorId() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "Maria Souza",
                  "cpf": "98765432100",
                  "email": "maria.souza@example.com",
                  "telefone": "11988887777"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/responsaveis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nomeCompleto").value("Maria Souza"))
                .andExpect(jsonPath("$.cpf").value("98765432100"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get("/api/responsaveis/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nomeCompleto").value("Maria Souza"));
    }

    @Test
    @WithMockUser
    void deveRetornarConflitoQuandoCpfDuplicado() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "Carlos Lima",
                  "cpf": "12312312399",
                  "email": "carlos@example.com",
                  "telefone": "11999998888"
                }
                """;

        mockMvc.perform(post("/api/responsaveis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/responsaveis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe responsável cadastrado com este CPF"));
    }
}
