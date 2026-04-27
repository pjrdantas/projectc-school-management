package br.com.escola.studentmanagement.adapter.in.web;

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

@SpringBootTest
@AutoConfigureMockMvc
class AlunoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void deveBuscarAlunoPorIdQuandoExistir() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "João da Silva",
                  "cpf": "12345678901",
                  "email": "joao.silva@example.com",
                  "dataNascimento": "2010-05-15"
                }
                """;

        @SuppressWarnings("null")
		String responseBody = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = responseBody.replaceAll(".*\\\"id\\\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/alunos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Long.parseLong(id)))
                .andExpect(jsonPath("$.nomeCompleto").value("João da Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"));
    }

    @Test
    @WithMockUser
    void deveRetornarNotFoundQuandoAlunoNaoExistir() throws Exception {
        mockMvc.perform(get("/api/alunos/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado para o id 99999"));
    }
}
