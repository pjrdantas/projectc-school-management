package br.com.escola.professor.adapter.in.web.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.professor.adapter.in.web.dto.DiarioClasseAlunoResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseAssinaturaResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseAvaliacaoResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseCabecalhoResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseConteudoPlanejadoResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseSalvarRequest;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseSalvarResponse;
import br.com.escola.professor.application.service.DiarioClasseConsultaService;
import com.fasterxml.jackson.databind.ObjectMapper;

class DiarioClasseInternalControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deveCarregarDiarioClasseNoContratoInterno() throws Exception {
        DiarioClasseConsultaService diarioClasseConsultaService = Mockito.mock(DiarioClasseConsultaService.class);
        UUID professorId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();

        when(diarioClasseConsultaService.carregar(
                professorId,
                turmaId,
                disciplinaId,
                2058,
                6,
                java.time.LocalDate.of(2058, 6, 26)))
                .thenReturn(new DiarioClasseResponse(
                        new DiarioClasseCabecalhoResponse(
                                "diario-2058-06-x",
                                UUID.randomUUID().toString(),
                                "Escola Interna",
                                "Diretoria Interna",
                                "Municipio Interno",
                                2058,
                                6,
                                "26/06",
                                turmaId.toString(),
                                "Turma A",
                                "MANHA",
                                disciplinaId.toString(),
                                "Matematica",
                                professorId.toString(),
                                "Professor Interno"),
                        List.of(new DiarioClasseAlunoResponse(
                                UUID.randomUUID().toString(),
                                1,
                                "Aluno Interno",
                                Map.of("12", "P"))),
                        List.of(new DiarioClasseConteudoPlanejadoResponse(
                                UUID.randomUUID().toString(),
                                "Aula 1",
                                "Conteudo planejado")),
                        List.of("Observacao interna"),
                        List.of(new DiarioClasseAvaliacaoResponse(
                                UUID.randomUUID().toString(),
                                "12/06",
                                "Prova mensal",
                                "Turma A",
                                "0 a 10")),
                        new DiarioClasseAssinaturaResponse("Professor Interno", "26/06/2058"),
                        false));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DiarioClasseInternalController(diarioClasseConsultaService)).build();

        mockMvc.perform(get("/internal/diarios-classe")
                        .param("idProfessor", professorId.toString())
                        .param("idTurma", turmaId.toString())
                        .param("idDisciplina", disciplinaId.toString())
                        .param("anoLetivo", "2058")
                        .param("mes", "6")
                        .param("dataReferencia", "2058-06-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cabecalho.idProfessor").value(professorId.toString()))
                .andExpect(jsonPath("$.cabecalho.idTurma").value(turmaId.toString()))
                .andExpect(jsonPath("$.cabecalho.idDisciplina").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.alunos[0].nome").value("Aluno Interno"))
                .andExpect(jsonPath("$.alunos[0].frequencias.12").value("P"))
                .andExpect(jsonPath("$.conteudosPlanejados[0].descricao").value("Conteudo planejado"))
                .andExpect(jsonPath("$.avaliacoes[0].descricao").value("Prova mensal"))
                .andExpect(jsonPath("$.bloqueado").value(false));

        verify(diarioClasseConsultaService).carregar(
                professorId,
                turmaId,
                disciplinaId,
                2058,
                6,
                java.time.LocalDate.of(2058, 6, 26));
    }

    @Test
    void deveSalvarDiarioClasseNoContratoInterno() throws Exception {
        DiarioClasseConsultaService diarioClasseConsultaService = Mockito.mock(DiarioClasseConsultaService.class);
        String idDiarioClasse = "diario-2058-06-x";

        when(diarioClasseConsultaService.salvar(Mockito.eq(idDiarioClasse), Mockito.any(DiarioClasseSalvarRequest.class)))
                .thenReturn(new DiarioClasseSalvarResponse(
                        idDiarioClasse,
                        "SALVO",
                        "Lancamento salvo com sucesso.",
                        LocalDateTime.of(2058, 6, 26, 10, 30),
                        true));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DiarioClasseInternalController(diarioClasseConsultaService)).build();

        mockMvc.perform(put("/internal/diarios-classe/{idDiarioClasse}", idDiarioClasse)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSalvar(idDiarioClasse))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDiarioClasse").value(idDiarioClasse))
                .andExpect(jsonPath("$.status").value("SALVO"))
                .andExpect(jsonPath("$.bloqueado").value(true));

        verify(diarioClasseConsultaService).salvar(Mockito.eq(idDiarioClasse), Mockito.any(DiarioClasseSalvarRequest.class));
    }

    private DiarioClasseSalvarRequest requestSalvar(String idDiarioClasse) {
        return new DiarioClasseSalvarRequest(
                idDiarioClasse,
                LocalDate.of(2058, 6, 26),
                List.of(new br.com.escola.professor.adapter.in.web.dto.DiarioClasseFrequenciaRequest(
                        UUID.randomUUID(),
                        LocalDate.of(2058, 6, 26),
                        26,
                        "PRESENTE")),
                List.of(new br.com.escola.professor.adapter.in.web.dto.DiarioClasseConteudoRequest(
                        UUID.randomUUID().toString(),
                        "Aula 1",
                        "Conteudo ministrado",
                        false,
                        null)),
                List.of("Observacao interna"),
                new br.com.escola.professor.adapter.in.web.dto.DiarioClasseAssinaturaRequest(
                        "Professor Interno",
                        "26/06/2058"));
    }
}
