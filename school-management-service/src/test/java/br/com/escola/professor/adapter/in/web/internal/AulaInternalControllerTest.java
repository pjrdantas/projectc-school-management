package br.com.escola.professor.adapter.in.web.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.professor.adapter.in.web.dto.AulaRequest;
import br.com.escola.professor.adapter.in.web.dto.AulaResponse;
import br.com.escola.professor.application.service.DiarioAulaService;
import com.fasterxml.jackson.databind.ObjectMapper;

class AulaInternalControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deveCriarAulaNoContratoInterno() throws Exception {
        DiarioAulaService diarioAulaService = Mockito.mock(DiarioAulaService.class);
        UUID aulaId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();

        when(diarioAulaService.criarAula(Mockito.any(AulaRequest.class))).thenReturn(aulaResponse(aulaId, alocacaoId, "Turma Aula"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AulaInternalController(diarioAulaService)).build();

        mockMvc.perform(post("/internal/aulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aulaRequest(alocacaoId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(aulaId.toString()))
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Aula"));

        verify(diarioAulaService).criarAula(Mockito.any(AulaRequest.class));
    }

    @Test
    void deveListarAulasNoContratoInterno() throws Exception {
        DiarioAulaService diarioAulaService = Mockito.mock(DiarioAulaService.class);
        UUID alocacaoId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID aulaId = UUID.randomUUID();

        when(diarioAulaService.listarAulas(alocacaoId, turmaId)).thenReturn(List.of(aulaResponse(aulaId, alocacaoId, "Turma Interna")));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AulaInternalController(diarioAulaService)).build();

        mockMvc.perform(get("/internal/aulas")
                        .param("professorTurmaDisciplinaId", alocacaoId.toString())
                        .param("turmaId", turmaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(aulaId.toString()))
                .andExpect(jsonPath("$[0].professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$[0].turmaNome").value("Turma Interna"));

        verify(diarioAulaService).listarAulas(alocacaoId, turmaId);
    }

    @Test
    void deveBuscarAulaPorIdNoContratoInterno() throws Exception {
        DiarioAulaService diarioAulaService = Mockito.mock(DiarioAulaService.class);
        UUID aulaId = UUID.randomUUID();
        UUID alocacaoId = UUID.randomUUID();

        when(diarioAulaService.buscarAulaPorId(aulaId)).thenReturn(aulaResponse(aulaId, alocacaoId, "Turma Detalhe"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AulaInternalController(diarioAulaService)).build();

        mockMvc.perform(get("/internal/aulas/{id}", aulaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aulaId.toString()))
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Detalhe"));

        verify(diarioAulaService).buscarAulaPorId(aulaId);
    }

    private AulaRequest aulaRequest(UUID alocacaoId) {
        return new AulaRequest(
                alocacaoId,
                LocalDate.of(2038, 3, 10),
                LocalTime.of(7, 30),
                LocalTime.of(8, 20),
                "Conteudo interno",
                "Observacao interna",
                true);
    }

    private AulaResponse aulaResponse(UUID aulaId, UUID alocacaoId, String turmaNome) {
        return new AulaResponse(
                aulaId,
                alocacaoId,
                UUID.randomUUID(),
                "Professor Interno",
                UUID.randomUUID(),
                turmaNome,
                UUID.randomUUID(),
                "Escola Interna",
                UUID.randomUUID(),
                "Matematica",
                LocalDate.of(2038, 3, 10),
                LocalTime.of(7, 30),
                LocalTime.of(8, 20),
                "Conteudo interno",
                "Observacao interna",
                true,
                LocalDateTime.of(2038, 3, 10, 7, 0));
    }
}
