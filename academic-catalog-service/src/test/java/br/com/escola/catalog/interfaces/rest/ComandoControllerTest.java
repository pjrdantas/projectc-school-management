package br.com.escola.catalog.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.catalog.application.command.CommandResult;
import br.com.escola.catalog.application.command.CreateDisciplinaCommand;
import br.com.escola.catalog.application.command.UpdateDisciplinaCommand;
import br.com.escola.catalog.application.command.UpdatePeriodoLetivoCommand;
import br.com.escola.catalog.application.command.UpdateSerieCommand;
import br.com.escola.catalog.application.command.UpdateTurmaCommand;
import br.com.escola.catalog.application.command.UpdateTurmaDisciplinaCommand;
import br.com.escola.catalog.application.context.InternalHeaders;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.port.in.ComandoUseCase;
import br.com.escola.catalog.infra.config.InternalApiWebConfiguration;
import br.com.escola.catalog.infra.security.InternalApiInterceptor;
import br.com.escola.catalog.interfaces.advice.ApiExceptionHandler;

@WebMvcTest(ComandoController.class)
@Import({InternalApiWebConfiguration.class, InternalApiInterceptor.class, ApiExceptionHandler.class})
@TestPropertySource(properties = "catalog.internal-api.token=test-internal-token")
class ComandoControllerTest {

    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComandoUseCase commandUseCase;

    @Test
    void deveExigirIdempotencyKey() throws Exception {
        mockMvc.perform(authenticatedPost().content("{\"nome\":\"Matematica\",\"cargaHoraria\":80}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void deveCriarComContextoTenantEInformarReplay() throws Exception {
        UUID disciplinaId = UUID.randomUUID();
        when(commandUseCase.criarDisciplina(any(), eq("command-key"), any()))
                .thenReturn(new CommandResult<>(new DisciplinaResponse(
                        disciplinaId, "Matematica", 80, true, ESCOLA_ID, LocalDateTime.now()), true));

        mockMvc.perform(authenticatedPost()
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "command-key")
                        .content("{\"nome\":\"Matematica\",\"cargaHoraria\":80}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "true"))
                .andExpect(jsonPath("$.id").value(disciplinaId.toString()));

        verify(commandUseCase).criarDisciplina(
                eq(new CreateDisciplinaCommand("Matematica", 80, null)),
                eq("command-key"),
                any(InternalRequestContext.class));
    }

    @Test
    void deveAtualizarPeriodoNoContextoTenant() throws Exception {
        UUID periodoId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2026, 2, 1);
        LocalDate dataFim = LocalDate.of(2026, 12, 15);
        when(commandUseCase.atualizarPeriodo(eq(periodoId), any(), eq("update-key"), any()))
                .thenReturn(new CommandResult<>(new PeriodoLetivoResponse(
                        periodoId, "Letivo 2026", 2026, dataInicio, dataFim, true, ESCOLA_ID, LocalDateTime.now()), false));

        mockMvc.perform(put("/internal/v1/periodos-letivos/{periodoId}", periodoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-update")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "update-key")
                        .content("""
                                {"nome":"Letivo 2026","ano":2026,"dataInicio":"2026-02-01","dataFim":"2026-12-15","ativo":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "false"))
                .andExpect(jsonPath("$.id").value(periodoId.toString()))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(commandUseCase).atualizarPeriodo(
                eq(periodoId),
                eq(new UpdatePeriodoLetivoCommand("Letivo 2026", 2026, dataInicio, dataFim, true)),
                eq("update-key"),
                any(InternalRequestContext.class));
    }

    @Test
    void deveAtualizarDisciplinaNoContextoTenant() throws Exception {
        UUID disciplinaId = UUID.randomUUID();
        when(commandUseCase.atualizarDisciplina(eq(disciplinaId), any(), eq("update-subject-key"), any()))
                .thenReturn(new CommandResult<>(new DisciplinaResponse(
                        disciplinaId, "Matematica Aplicada", 100, false, ESCOLA_ID, LocalDateTime.now()), false));

        mockMvc.perform(put("/internal/v1/disciplinas/{disciplinaId}", disciplinaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-update-subject")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "update-subject-key")
                        .content("{\"nome\":\"Matematica Aplicada\",\"cargaHoraria\":100,\"ativo\":false}"))
                .andExpect(status().isOk())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "false"))
                .andExpect(jsonPath("$.id").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.ativo").value(false));

        verify(commandUseCase).atualizarDisciplina(
                eq(disciplinaId),
                eq(new UpdateDisciplinaCommand("Matematica Aplicada", 100, false)),
                eq("update-subject-key"),
                any(InternalRequestContext.class));
    }

    @Test
    void deveAtualizarSerieNoContextoTenant() throws Exception {
        UUID serieId = UUID.randomUUID();
        UUID nivelEnsinoId = UUID.randomUUID();
        when(commandUseCase.atualizarSerie(eq(serieId), any(), eq("update-grade-key"), any()))
                .thenReturn(new CommandResult<>(new SerieResponse(
                        serieId, "2o ano", 2, nivelEnsinoId, "FUNDAMENTAL", ESCOLA_ID, LocalDateTime.now()), false));

        mockMvc.perform(put("/internal/v1/series/{serieId}", serieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-update-grade")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "update-grade-key")
                        .content("{\"nome\":\"2o ano\",\"ordem\":2,\"nivelEnsinoId\":\"" + nivelEnsinoId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "false"))
                .andExpect(jsonPath("$.id").value(serieId.toString()))
                .andExpect(jsonPath("$.ordem").value(2));

        verify(commandUseCase).atualizarSerie(
                eq(serieId),
                eq(new UpdateSerieCommand("2o ano", 2, nivelEnsinoId)),
                eq("update-grade-key"),
                any(InternalRequestContext.class));
    }

    @Test
    void deveAtualizarTurmaNoContextoTenant() throws Exception {
        UUID turmaId = UUID.randomUUID();
        UUID periodoId = UUID.randomUUID();
        UUID serieId = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();
        when(commandUseCase.atualizarTurma(eq(turmaId), any(), eq("update-class-key"), any()))
                .thenReturn(new CommandResult<>(new TurmaResponse(
                        turmaId, "2A", "2o Ano A", 32, periodoId, serieId, "2o ano", turnoId,
                        "MANHA", false, ESCOLA_ID, LocalDateTime.now()), false));

        mockMvc.perform(put("/internal/v1/turmas/{turmaId}", turmaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-update-class")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "update-class-key")
                        .content("{\"codigo\":\"2A\",\"nome\":\"2o Ano A\",\"capacidade\":32,"
                                + "\"periodoLetivoId\":\"" + periodoId + "\",\"serieId\":\"" + serieId
                                + "\",\"turnoId\":\"" + turnoId + "\",\"ativo\":false}"))
                .andExpect(status().isOk())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "false"))
                .andExpect(jsonPath("$.id").value(turmaId.toString()))
                .andExpect(jsonPath("$.ativo").value(false));

        verify(commandUseCase).atualizarTurma(
                eq(turmaId),
                eq(new UpdateTurmaCommand("2A", "2o Ano A", 32, periodoId, serieId, turnoId, false)),
                eq("update-class-key"),
                any(InternalRequestContext.class));
    }

    @Test
    void deveExcluirPeriodoNoContextoTenant() throws Exception {
        UUID periodoId = UUID.randomUUID();
        when(commandUseCase.excluirPeriodo(eq(periodoId), eq("delete-key"), any()))
                .thenReturn(new CommandResult<>(null, true));

        mockMvc.perform(delete("/internal/v1/periodos-letivos/{periodoId}", periodoId)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-delete")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "delete-key"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "true"));

        verify(commandUseCase).excluirPeriodo(eq(periodoId), eq("delete-key"), any(InternalRequestContext.class));
    }

    @Test
    void deveExcluirDisciplinaNoContextoTenant() throws Exception {
        UUID disciplinaId = UUID.randomUUID();
        when(commandUseCase.excluirDisciplina(eq(disciplinaId), eq("delete-subject-key"), any()))
                .thenReturn(new CommandResult<>(null, false));

        mockMvc.perform(delete("/internal/v1/disciplinas/{disciplinaId}", disciplinaId)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-delete-subject")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "delete-subject-key"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "false"));

        verify(commandUseCase).excluirDisciplina(
                eq(disciplinaId), eq("delete-subject-key"), any(InternalRequestContext.class));
    }

    @Test
    void deveExcluirSerieNoContextoTenant() throws Exception {
        UUID serieId = UUID.randomUUID();
        when(commandUseCase.excluirSerie(eq(serieId), eq("delete-grade-key"), any()))
                .thenReturn(new CommandResult<>(null, true));

        mockMvc.perform(delete("/internal/v1/series/{serieId}", serieId)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-delete-grade")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "delete-grade-key"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "true"));

        verify(commandUseCase).excluirSerie(
                eq(serieId), eq("delete-grade-key"), any(InternalRequestContext.class));
    }

    @Test
    void deveExcluirTurmaNoContextoTenant() throws Exception {
        UUID turmaId = UUID.randomUUID();
        when(commandUseCase.excluirTurma(eq(turmaId), eq("delete-class-key"), any()))
                .thenReturn(new CommandResult<>(null, false));

        mockMvc.perform(delete("/internal/v1/turmas/{turmaId}", turmaId)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-delete-class")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "delete-class-key"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "false"));

        verify(commandUseCase).excluirTurma(eq(turmaId), eq("delete-class-key"), any(InternalRequestContext.class));
    }

    @Test
    void deveAtualizarVinculoDisciplinaNoContextoTenant() throws Exception {
        UUID turmaId = UUID.randomUUID();
        UUID vinculoId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        when(commandUseCase.atualizarVinculoDisciplina(eq(turmaId), eq(vinculoId), any(), eq("update-link-key"), any()))
                .thenReturn(new CommandResult<>(new TurmaDisciplinaResponse(
                        vinculoId, turmaId, disciplinaId, "Matematica", 100, ESCOLA_ID, LocalDateTime.now()), false));

        mockMvc.perform(put("/internal/v1/turmas/{turmaId}/disciplinas/{vinculoId}", turmaId, vinculoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-update-link")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "update-link-key")
                        .content("{\"cargaHoraria\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vinculoId.toString()))
                .andExpect(jsonPath("$.cargaHoraria").value(100));

        verify(commandUseCase).atualizarVinculoDisciplina(
                eq(turmaId), eq(vinculoId), eq(new UpdateTurmaDisciplinaCommand(100)),
                eq("update-link-key"), any(InternalRequestContext.class));
    }

    @Test
    void deveDesvincularDisciplinaNoContextoTenant() throws Exception {
        UUID turmaId = UUID.randomUUID();
        UUID vinculoId = UUID.randomUUID();
        when(commandUseCase.desvincularDisciplina(eq(turmaId), eq(vinculoId), eq("unlink-key"), any()))
                .thenReturn(new CommandResult<>(null, true));

        mockMvc.perform(delete("/internal/v1/turmas/{turmaId}/disciplinas/{vinculoId}", turmaId, vinculoId)
                        .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                        .header(InternalHeaders.CORRELATION_ID, "corr-unlink")
                        .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                        .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID)
                        .header(InternalHeaders.IDEMPOTENCY_KEY, "unlink-key"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(InternalHeaders.IDEMPOTENCY_REPLAYED, "true"));

        verify(commandUseCase).desvincularDisciplina(
                eq(turmaId), eq(vinculoId), eq("unlink-key"), any(InternalRequestContext.class));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedPost() {
        return post("/internal/v1/disciplinas")
                .contentType(MediaType.APPLICATION_JSON)
                .header(InternalHeaders.INTERNAL_TOKEN, "test-internal-token")
                .header(InternalHeaders.CORRELATION_ID, "corr-command")
                .header(InternalHeaders.USUARIO_ID, USUARIO_ID)
                .header(InternalHeaders.ESCOLA_ID, ESCOLA_ID);
    }
}

