package br.com.escola.avaliacao.adapter.in.web.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.avaliacao.adapter.in.web.dto.AvaliacaoRequest;
import br.com.escola.avaliacao.adapter.in.web.dto.AvaliacaoResponse;
import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoRequest;
import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoResponse;
import br.com.escola.avaliacao.application.service.AvaliacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;

class AvaliacaoInternalControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deveCriarAvaliacaoNoContratoInterno() throws Exception {
        AvaliacaoService avaliacaoService = Mockito.mock(AvaliacaoService.class);
        UUID avaliacaoId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();

        when(avaliacaoService.criar(Mockito.any(AvaliacaoRequest.class)))
                .thenReturn(avaliacaoResponse(avaliacaoId, alocacaoId, "Turma Avaliacao"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AvaliacaoInternalController(avaliacaoService)).build();

        mockMvc.perform(post("/internal/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(avaliacaoRequest(alocacaoId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Avaliacao"));

        verify(avaliacaoService).criar(Mockito.any(AvaliacaoRequest.class));
    }

    @Test
    void deveListarAvaliacoesNoContratoInterno() throws Exception {
        AvaliacaoService avaliacaoService = Mockito.mock(AvaliacaoService.class);
        UUID avaliacaoId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        when(avaliacaoService.listar(alocacaoId, turmaId))
                .thenReturn(List.of(avaliacaoResponse(avaliacaoId, alocacaoId, "Turma Interna")));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AvaliacaoInternalController(avaliacaoService)).build();

        mockMvc.perform(get("/internal/avaliacoes")
                        .param("professorTurmaDisciplinaId", alocacaoId.toString())
                        .param("turmaId", turmaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$[0].professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$[0].turmaNome").value("Turma Interna"));

        verify(avaliacaoService).listar(alocacaoId, turmaId);
    }

    @Test
    void deveBuscarAvaliacaoPorIdNoContratoInterno() throws Exception {
        AvaliacaoService avaliacaoService = Mockito.mock(AvaliacaoService.class);
        UUID avaliacaoId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();

        when(avaliacaoService.buscarPorId(avaliacaoId))
                .thenReturn(avaliacaoResponse(avaliacaoId, alocacaoId, "Turma Detalhe"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AvaliacaoInternalController(avaliacaoService)).build();

        mockMvc.perform(get("/internal/avaliacoes/{id}", avaliacaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Detalhe"));

        verify(avaliacaoService).buscarPorId(avaliacaoId);
    }

    @Test
    void deveLancarNotaNoContratoInterno() throws Exception {
        AvaliacaoService avaliacaoService = Mockito.mock(AvaliacaoService.class);
        UUID avaliacaoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        when(avaliacaoService.lancarNota(Mockito.eq(avaliacaoId), Mockito.any(NotaAlunoRequest.class)))
                .thenReturn(notaAlunoResponse(avaliacaoId, matriculaId, "Aluno Interno"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AvaliacaoInternalController(avaliacaoService)).build();

        mockMvc.perform(post("/internal/avaliacoes/{id}/notas", avaliacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NotaAlunoRequest(
                                matriculaId,
                                new BigDecimal("8.50"),
                                "Boa participacao"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.avaliacaoId").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Interno"));

        verify(avaliacaoService).lancarNota(Mockito.eq(avaliacaoId), Mockito.any(NotaAlunoRequest.class));
    }

    @Test
    void deveListarNotasPorAvaliacaoNoContratoInterno() throws Exception {
        AvaliacaoService avaliacaoService = Mockito.mock(AvaliacaoService.class);
        UUID avaliacaoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        when(avaliacaoService.listarNotasPorAvaliacao(avaliacaoId))
                .thenReturn(List.of(notaAlunoResponse(avaliacaoId, matriculaId, "Aluno Interno")));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AvaliacaoInternalController(avaliacaoService)).build();

        mockMvc.perform(get("/internal/avaliacoes/{id}/notas", avaliacaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].avaliacaoId").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].alunoNome").value("Aluno Interno"));

        verify(avaliacaoService).listarNotasPorAvaliacao(avaliacaoId);
    }

    private AvaliacaoRequest avaliacaoRequest(UUID alocacaoId) {
        return new AvaliacaoRequest(
                alocacaoId,
                "Prova interna",
                "Descricao interna",
                LocalDate.of(2039, 4, 15),
                new BigDecimal("10.00"),
                new BigDecimal("1.00"),
                "PROVA");
    }

    private AvaliacaoResponse avaliacaoResponse(UUID avaliacaoId, UUID alocacaoId, String turmaNome) {
        return new AvaliacaoResponse(
                avaliacaoId,
                alocacaoId,
                UUID.randomUUID(),
                "Professor Interno",
                UUID.randomUUID(),
                turmaNome,
                UUID.randomUUID(),
                "Escola Interna",
                UUID.randomUUID(),
                "Matematica",
                "Prova interna",
                "Descricao interna",
                LocalDate.of(2039, 4, 15),
                new BigDecimal("10.00"),
                new BigDecimal("1.00"),
                "PROVA",
                LocalDateTime.of(2039, 4, 15, 7, 0));
    }

    private NotaAlunoResponse notaAlunoResponse(UUID avaliacaoId, UUID matriculaId, String alunoNome) {
        return new NotaAlunoResponse(
                UUID.randomUUID(),
                avaliacaoId,
                "Prova interna",
                matriculaId,
                UUID.randomUUID(),
                alunoNome,
                UUID.randomUUID(),
                "Escola Interna",
                new BigDecimal("8.50"),
                "Boa participacao",
                LocalDateTime.of(2039, 4, 15, 8, 0),
                LocalDateTime.of(2039, 4, 15, 8, 5));
    }
}
