package br.com.escola.avaliacao.adapter.in.web.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoResponse;
import br.com.escola.avaliacao.application.service.AvaliacaoService;

class NotaAlunoInternalControllerTest {

    @Test
    void deveListarNotasPorMatriculaNoContratoInterno() throws Exception {
        AvaliacaoService avaliacaoService = Mockito.mock(AvaliacaoService.class);
        UUID matriculaId = UUID.randomUUID();

        when(avaliacaoService.listarNotasPorMatricula(matriculaId))
                .thenReturn(List.of(notaAlunoResponse(matriculaId)));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new NotaAlunoInternalController(avaliacaoService)).build();

        mockMvc.perform(get("/internal/matriculas/{matriculaId}/notas", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].alunoNome").value("Aluno Matricula"));

        verify(avaliacaoService).listarNotasPorMatricula(matriculaId);
    }

    private NotaAlunoResponse notaAlunoResponse(UUID matriculaId) {
        return new NotaAlunoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Prova interna",
                matriculaId,
                UUID.randomUUID(),
                "Aluno Matricula",
                UUID.randomUUID(),
                "Escola Interna",
                new BigDecimal("9.00"),
                "Otimo desempenho",
                LocalDateTime.of(2039, 4, 15, 8, 0),
                LocalDateTime.of(2039, 4, 15, 8, 5));
    }
}
