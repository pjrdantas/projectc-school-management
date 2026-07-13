package br.com.escola.documento.adapter.in.web.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.usecase.ListarDocumentosPorEntidadeUseCase;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentoInternalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListarDocumentosPorEntidadeUseCase listarDocumentosPorEntidadeUseCase;

    @Test
    @WithMockUser
    void deveListarDocumentosInternosPorEntidade() throws Exception {
        UUID entidadeId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        when(listarDocumentosPorEntidadeUseCase.executar(eq("MATRICULA"), any()))
                .thenReturn(List.of(new DocumentoOutput(
                        documentoId,
                        "MATRICULA",
                        entidadeId,
                        UUID.fromString("00000000-0000-0000-0000-000000000047"),
                        "Escola padrao",
                        "CPF",
                        "12345678900",
                        "s3://bucket/documento.pdf",
                        LocalDateTime.of(2026, 7, 12, 10, 0),
                        "Documento administrativo")));

        mockMvc.perform(get("/internal/documentos")
                        .param("entidadeTipo", "MATRICULA")
                        .param("entidadeId", entidadeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(documentoId.toString()))
                .andExpect(jsonPath("$[0].entidadeTipo").value("MATRICULA"))
                .andExpect(jsonPath("$[0].entidadeId").value(entidadeId.toString()))
                .andExpect(jsonPath("$[0].tipoDocumento").value("CPF"));
    }

    @Test
    void deveExigirAutenticacaoNosDocumentosInternos() throws Exception {
        mockMvc.perform(get("/internal/documentos")
                        .param("entidadeTipo", "MATRICULA")
                        .param("entidadeId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }
}
