package br.com.escola.matricula.adapter.in.web.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import br.com.escola.matricula.application.dto.MatriculaEtapaOutput;
import br.com.escola.matricula.application.dto.MatriculaOutput;
import br.com.escola.matricula.application.usecase.ConsultarMatriculasUseCase;

@SpringBootTest
@AutoConfigureMockMvc
class MatriculaInternalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultarMatriculasUseCase consultarMatriculasUseCase;

    @Test
    @WithMockUser
    void deveListarMatriculasInternasComEtapas() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID serieId = UUID.randomUUID();
        UUID periodoLetivoId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();

        when(consultarMatriculasUseCase.executar(any()))
                .thenReturn(List.of(new MatriculaOutput(
                        matriculaId,
                        alunoId,
                        turmaId,
                        escolaId,
                        "Escola padrao",
                        serieId,
                        "6 Ano",
                        periodoLetivoId,
                        "EM_ANDAMENTO",
                        "PRIMEIRA_MATRICULA",
                        LocalDate.of(2026, 7, 12),
                        "Matricula interna",
                        LocalDateTime.of(2026, 7, 12, 10, 0),
                        List.of(new MatriculaEtapaOutput(
                                etapaId,
                                "Analise documental",
                                1,
                                "PENDENTE",
                                LocalDateTime.of(2026, 7, 12, 10, 0),
                                null,
                                "Aguardando conferencia")))));

        mockMvc.perform(get("/internal/matriculas")
                        .param("alunoId", alunoId.toString())
                        .param("status", "EM_ANDAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$[0].status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$[0].etapas[0].id").value(etapaId.toString()))
                .andExpect(jsonPath("$[0].etapas[0].descricao").value("Analise documental"));
    }

    @Test
    void deveExigirAutenticacaoNasMatriculasInternas() throws Exception {
        mockMvc.perform(get("/internal/matriculas"))
                .andExpect(status().isUnauthorized());
    }
}
