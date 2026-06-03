package br.com.escola.responsavel.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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

    @Test
    @WithMockUser
    void naoDeveExcluirResponsavelVinculadoAAluno() throws Exception {
        UUID alunoId = criarAluno();
        String responsavelId = criarResponsavel("Responsavel Vinculado", cpfAleatorio());

        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis/{idResponsavel}", alunoId, responsavelId))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/responsaveis/{id}", responsavelId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Responsável não pode ser excluído porque ainda está vinculado a outro aluno: " + responsavelId));
    }

    private UUID criarAluno() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "Aluno Responsavel",
                  "cpf": "%s",
                  "email": "aluno.responsavel@example.com",
                  "dataNascimento": "2010-05-15"
                }
                """.formatted(cpfAleatorio());

        String responseBody = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private String criarResponsavel(String nome, String cpf) throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "%s",
                  "cpf": "%s",
                  "email": "responsavel.vinculado@example.com",
                  "telefone": "11999998888"
                }
                """.formatted(nome, cpf);

        String responseBody = mockMvc.perform(post("/api/responsaveis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asText();
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
