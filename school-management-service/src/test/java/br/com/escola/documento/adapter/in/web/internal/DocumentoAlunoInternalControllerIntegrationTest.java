package br.com.escola.documento.adapter.in.web.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.documento.adapter.in.web.dto.DocumentoAlunoResponse;
import br.com.escola.documento.application.service.DocumentoAlunoService;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentoAlunoInternalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentoAlunoService documentoAlunoService;

    @Test
    @WithMockUser
    void deveListarDocumentosInternosPorAluno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        when(documentoAlunoService.listarPorAluno(any()))
                .thenReturn(List.of(new DocumentoAlunoResponse(
                        documentoId,
                        alunoId,
                        "HISTORICO_ESCOLAR",
                        "historico.pdf",
                        "s3://bucket/historico.pdf",
                        "historico.pdf",
                        "s3://bucket/historico.pdf",
                        LocalDateTime.of(2026, 7, 12, 10, 0),
                        "Documento escolar")));

        mockMvc.perform(get("/internal/documentos-alunos/alunos/{alunoId}", alunoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(documentoId.toString()))
                .andExpect(jsonPath("$[0].alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$[0].tipoDocumento").value("HISTORICO_ESCOLAR"));
    }

    @Test
    void deveExigirAutenticacaoNosDocumentosInternos() throws Exception {
        mockMvc.perform(get("/internal/documentos-alunos/alunos/{alunoId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
