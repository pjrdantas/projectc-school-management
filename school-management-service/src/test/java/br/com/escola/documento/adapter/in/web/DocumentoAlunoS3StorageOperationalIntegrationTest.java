package br.com.escola.documento.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@SpringBootTest(properties = {
        "documento.storage.backend=s3",
        "documento.storage.s3.bucket=documentos-operacional",
        "documento.storage.s3.prefix=validacao"
})
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM pessoa_documento",
                "DELETE FROM documento",
                "DELETE FROM transferencia_aluno",
                "DELETE FROM escola WHERE id_escola <> '00000000-0000-0000-0000-000000000047'",
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
class DocumentoAlunoS3StorageOperationalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private S3Client s3Client;

    @Test
    @WithMockUser
    void deveCadastrarDocumentoDoAlunoComUploadUsandoBackendS3Configurado() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("etag-operacional").build());
        UUID alunoId = criarAluno();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "historico-operacional.pdf",
                "application/pdf",
                "conteudo operacional".getBytes());

        MockHttpServletRequestBuilder request = multipart("/api/documentos-alunos")
                .file(arquivo)
                .param("alunoId", alunoId.toString())
                .param("tipoDocumento", "HISTORICO_ESCOLAR")
                .param("numeroDocumento", "historico-operacional.pdf")
                .param("observacao", "Validacao operacional S3")
                .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.tipoDocumento").value("HISTORICO_ESCOLAR"))
                .andExpect(jsonPath("$.nomeArquivo").value("historico-operacional.pdf"))
                .andExpect(jsonPath("$.urlArquivo").value(org.hamcrest.Matchers.startsWith("s3://documentos-operacional/validacao/aluno/" + alunoId)))
                .andExpect(jsonPath("$.caminhoArquivo").value(org.hamcrest.Matchers.startsWith("s3://documentos-operacional/validacao/aluno/" + alunoId)));

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest putObjectRequest = requestCaptor.getValue();
        assertThat(putObjectRequest.bucket()).isEqualTo("documentos-operacional");
        assertThat(putObjectRequest.key()).startsWith("validacao/aluno/" + alunoId + "/");
        assertThat(putObjectRequest.key()).endsWith("-historico-operacional.pdf");
        assertThat(putObjectRequest.contentType()).isEqualTo("application/pdf");
    }

    private UUID criarAluno() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "Aluno Documento S3",
                  "cpf": "%s",
                  "email": "documento-s3@example.com",
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
