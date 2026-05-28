package br.com.escola.studentdocument.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM pessoa_documento",
                "DELETE FROM documento",
                "DELETE FROM transferencia_aluno",
                "DELETE FROM escola",
                "DELETE FROM historico_escolar_item",
                "DELETE FROM historico_escolar",
                "DELETE FROM disciplina",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM aluno_responsavel",
                "DELETE FROM turma",
                "DELETE FROM serie WHERE id_serie <> '00000000-0000-0000-0000-000000000100'",
                "DELETE FROM periodo_letivo",
                "DELETE FROM aluno"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DocumentoAlunoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void deveCadastrarDocumentoDoAlunoSomenteComMetadados() throws Exception {
        UUID alunoId = criarAluno();

        String requestBody = """
                {
                  "alunoId": "%s",
                  "tipoDocumento": "HISTORICO_ESCOLAR",
                  "nomeArquivo": "historico-escolar-2025.pdf",
                  "urlArquivo": "s3://school-documents/alunos/%s/historico-escolar-2025.pdf",
                  "observacao": "Documento enviado pela escola anterior"
                }
                """.formatted(alunoId, alunoId);

        String responseBody = mockMvc.perform(post("/api/documentos-alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.tipoDocumento").value("HISTORICO_ESCOLAR"))
                .andExpect(jsonPath("$.nomeArquivo").value("historico-escolar-2025.pdf"))
                .andExpect(jsonPath("$.urlArquivo").value("s3://school-documents/alunos/%s/historico-escolar-2025.pdf".formatted(alunoId)))
                .andExpect(jsonPath("$.dataUpload").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID documentoId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        mockMvc.perform(get("/api/documentos-alunos/{id}", documentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentoId.toString()));

        mockMvc.perform(get("/api/documentos-alunos/alunos/{alunoId}", alunoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(documentoId.toString()));

        mockMvc.perform(delete("/api/documentos-alunos/{id}", documentoId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deveRejeitarTipoDocumentoInvalido() throws Exception {
        UUID alunoId = criarAluno();

        String requestBody = """
                {
                  "alunoId": "%s",
                  "tipoDocumento": "BOLETIM",
                  "nomeArquivo": "boletim.pdf",
                  "urlArquivo": "/documentos/boletim.pdf"
                }
                """.formatted(alunoId);

        mockMvc.perform(post("/api/documentos-alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tipo de documento inválido: BOLETIM"));
    }

    private UUID criarAluno() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "Aluno Documento",
                  "cpf": "%s",
                  "email": "documento@example.com",
                  "telefone": "11999999999",
                  "dataNascimento": "2012-05-10"
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

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
