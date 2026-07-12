package br.com.escola.transferencia.adapter.in.web.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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

import br.com.escola.transferencia.application.dto.internal.EscolaOrigemResumo;
import br.com.escola.transferencia.application.dto.internal.TransferenciaAlunoResumo;
import br.com.escola.transferencia.application.service.TransferenciaAlunoService;

@SpringBootTest
@AutoConfigureMockMvc
class TransferenciaInternalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferenciaAlunoService transferenciaAlunoService;

    @Test
    @WithMockUser
    void deveExporTransferenciaInternaEEscolasOrigem() throws Exception {
        UUID transferenciaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID escolaOrigemId = UUID.randomUUID();

        when(transferenciaAlunoService.criarTransferencia(any()))
                .thenReturn(transferenciaResumo(transferenciaId, alunoId, escolaOrigemId));
        when(transferenciaAlunoService.listarEscolasOrigem())
                .thenReturn(List.of(escolaOrigemResumo(escolaOrigemId)));

        mockMvc.perform(post("/internal/transferencias")
                        .contentType("application/json")
                        .content("""
                                {
                                  "alunoId": "%s",
                                  "escolaOrigemId": "%s",
                                  "serieOrigem": "5A",
                                  "anoLetivoOrigem": "2026"
                                }
                                """.formatted(alunoId, escolaOrigemId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transferenciaId.toString()))
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()));

        mockMvc.perform(get("/internal/escolas-origem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(escolaOrigemId.toString()))
                .andExpect(jsonPath("$[0].nomeEscola").value("Escola Origem Interna"));
    }

    @Test
    @WithMockUser
    void deveBuscarTransferenciaInternaPorId() throws Exception {
        UUID transferenciaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID escolaOrigemId = UUID.randomUUID();
        when(transferenciaAlunoService.buscarTransferencia(any()))
                .thenReturn(transferenciaResumo(transferenciaId, alunoId, escolaOrigemId));

        mockMvc.perform(get("/internal/transferencias/{id}", transferenciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transferenciaId.toString()))
                .andExpect(jsonPath("$.escolaOrigem.id").value(escolaOrigemId.toString()));
    }

    @Test
    void deveExigirAutenticacaoNosAdaptadoresInternos() throws Exception {
        mockMvc.perform(get("/internal/escolas-origem"))
                .andExpect(status().isUnauthorized());
    }

    private TransferenciaAlunoResumo transferenciaResumo(UUID transferenciaId, UUID alunoId, UUID escolaOrigemId) {
        return new TransferenciaAlunoResumo(
                transferenciaId,
                alunoId,
                escolaOrigemResumo(escolaOrigemId),
                "5A",
                "2026",
                LocalDate.of(2026, 7, 1),
                "Mudanca",
                "ATIVO",
                "SIM",
                "ENTRADA",
                "EM_ANDAMENTO",
                "tester",
                "obs",
                LocalDateTime.of(2026, 7, 12, 10, 0),
                LocalDateTime.of(2026, 7, 12, 10, 0));
    }

    private EscolaOrigemResumo escolaOrigemResumo(UUID escolaOrigemId) {
        return new EscolaOrigemResumo(
                escolaOrigemId,
                "Escola Origem Interna",
                "123",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 7, 12, 10, 0));
    }
}
