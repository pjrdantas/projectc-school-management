package br.com.escola.historico.adapter.in.web.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.historico.adapter.in.web.dto.BoletimIndicadoresResponse;
import br.com.escola.historico.adapter.in.web.dto.BoletimItemResponse;
import br.com.escola.historico.adapter.in.web.dto.BoletimResponse;
import br.com.escola.historico.application.service.BoletimService;

class BoletimInternalControllerTest {

    @Test
    void deveConsultarBoletimPorMatriculaNoContratoInterno() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID boletimId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID periodoLetivoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();

        BoletimService boletimService = Mockito.mock(BoletimService.class);
        when(boletimService.consultarPorMatricula(matriculaId)).thenReturn(new BoletimResponse(
                boletimId,
                matriculaId,
                alunoId,
                "Aluno Interno",
                turmaId,
                "Turma A",
                periodoLetivoId,
                "2026",
                escolaId,
                "Escola Interna",
                LocalDate.of(2026, 7, 12),
                "2BIM",
                LocalDate.of(2026, 7, 12),
                "Observacao interna",
                true,
                new BoletimIndicadoresResponse(
                        1,
                        new BigDecimal("8.50"),
                        new BigDecimal("95.00"),
                        "APROVADO"),
                List.of(new BoletimItemResponse(
                        disciplinaId,
                        "Matematica",
                        new BigDecimal("8.50"),
                        new BigDecimal("95.00"),
                        4,
                        20,
                        "APROVADO"))));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new BoletimInternalController(boletimService)).build();

        mockMvc.perform(get("/internal/boletins/matriculas/{matriculaId}", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Interno"))
                .andExpect(jsonPath("$.itens[0].disciplinaNome").value("Matematica"))
                .andExpect(jsonPath("$.indicadores.resultadoGeral").value("APROVADO"));

        verify(boletimService).consultarPorMatricula(matriculaId);
    }
}
