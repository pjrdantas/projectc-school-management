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
import br.com.escola.professor.adapter.in.web.dto.FrequenciaAlunoRequest;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaAlunoResponse;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaProfessorRequest;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaProfessorResponse;
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

    @Test
    void deveRegistrarFrequenciaProfessorNoContratoInterno() throws Exception {
        DiarioAulaService diarioAulaService = Mockito.mock(DiarioAulaService.class);
        UUID aulaId = UUID.randomUUID();

        when(diarioAulaService.registrarFrequenciaProfessor(Mockito.eq(aulaId), Mockito.any(FrequenciaProfessorRequest.class)))
                .thenReturn(frequenciaProfessorResponse(aulaId));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AulaInternalController(diarioAulaService)).build();

        mockMvc.perform(post("/internal/aulas/{id}/frequencia-professor", aulaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FrequenciaProfessorRequest(true, "Presente"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$.professorNome").value("Professor Interno"));

        verify(diarioAulaService).registrarFrequenciaProfessor(Mockito.eq(aulaId), Mockito.any(FrequenciaProfessorRequest.class));
    }

    @Test
    void deveListarFrequenciaProfessorNoContratoInterno() throws Exception {
        DiarioAulaService diarioAulaService = Mockito.mock(DiarioAulaService.class);
        UUID aulaId = UUID.randomUUID();

        when(diarioAulaService.listarFrequenciaProfessor(aulaId)).thenReturn(List.of(frequenciaProfessorResponse(aulaId)));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AulaInternalController(diarioAulaService)).build();

        mockMvc.perform(get("/internal/aulas/{id}/frequencia-professor", aulaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$[0].professorNome").value("Professor Interno"));

        verify(diarioAulaService).listarFrequenciaProfessor(aulaId);
    }

    @Test
    void deveRegistrarFrequenciaAlunoNoContratoInterno() throws Exception {
        DiarioAulaService diarioAulaService = Mockito.mock(DiarioAulaService.class);
        UUID aulaId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        when(diarioAulaService.registrarFrequenciaAluno(Mockito.eq(aulaId), Mockito.any(FrequenciaAlunoRequest.class)))
                .thenReturn(frequenciaAlunoResponse(aulaId, matriculaId));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AulaInternalController(diarioAulaService)).build();

        mockMvc.perform(post("/internal/aulas/{id}/frequencias-alunos", aulaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FrequenciaAlunoRequest(matriculaId, "PRESENTE", "Participou"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Interno"));

        verify(diarioAulaService).registrarFrequenciaAluno(Mockito.eq(aulaId), Mockito.any(FrequenciaAlunoRequest.class));
    }

    @Test
    void deveListarFrequenciasAlunosNoContratoInterno() throws Exception {
        DiarioAulaService diarioAulaService = Mockito.mock(DiarioAulaService.class);
        UUID aulaId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();

        when(diarioAulaService.listarFrequenciasAlunos(aulaId)).thenReturn(List.of(frequenciaAlunoResponse(aulaId, matriculaId)));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AulaInternalController(diarioAulaService)).build();

        mockMvc.perform(get("/internal/aulas/{id}/frequencias-alunos", aulaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].alunoNome").value("Aluno Interno"));

        verify(diarioAulaService).listarFrequenciasAlunos(aulaId);
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

    private FrequenciaProfessorResponse frequenciaProfessorResponse(UUID aulaId) {
        return new FrequenciaProfessorResponse(
                UUID.randomUUID(),
                aulaId,
                UUID.randomUUID(),
                "Professor Interno",
                UUID.randomUUID(),
                "Escola Interna",
                true,
                "Presente",
                LocalDateTime.of(2038, 3, 10, 8, 0));
    }

    private FrequenciaAlunoResponse frequenciaAlunoResponse(UUID aulaId, UUID matriculaId) {
        return new FrequenciaAlunoResponse(
                UUID.randomUUID(),
                aulaId,
                matriculaId,
                UUID.randomUUID(),
                "Aluno Interno",
                UUID.randomUUID(),
                "Escola Interna",
                "PRESENTE",
                "Participou",
                LocalDateTime.of(2038, 3, 10, 8, 5));
    }
}
