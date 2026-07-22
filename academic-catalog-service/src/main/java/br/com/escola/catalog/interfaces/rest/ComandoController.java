package br.com.escola.catalog.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.catalog.application.command.CommandResult;
import br.com.escola.catalog.application.command.CreateDisciplinaCommand;
import br.com.escola.catalog.application.command.CreatePeriodoLetivoCommand;
import br.com.escola.catalog.application.command.CreateSerieCommand;
import br.com.escola.catalog.application.command.CreateTurmaCommand;
import br.com.escola.catalog.application.command.LinkDisciplinaCommand;
import br.com.escola.catalog.application.command.UpdateDisciplinaCommand;
import br.com.escola.catalog.application.command.UpdatePeriodoLetivoCommand;
import br.com.escola.catalog.application.command.UpdateSerieCommand;
import br.com.escola.catalog.application.command.UpdateTurmaCommand;
import br.com.escola.catalog.application.command.UpdateTurmaDisciplinaCommand;
import br.com.escola.catalog.application.context.InternalHeaders;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.port.in.ComandoUseCase;
import br.com.escola.catalog.interfaces.request.CreateDisciplinaRequest;
import br.com.escola.catalog.interfaces.request.CreatePeriodoLetivoRequest;
import br.com.escola.catalog.interfaces.request.CreateSerieRequest;
import br.com.escola.catalog.interfaces.request.CreateTurmaRequest;
import br.com.escola.catalog.interfaces.request.LinkDisciplinaRequest;
import br.com.escola.catalog.interfaces.request.UpdateDisciplinaRequest;
import br.com.escola.catalog.interfaces.request.UpdatePeriodoLetivoRequest;
import br.com.escola.catalog.interfaces.request.UpdateSerieRequest;
import br.com.escola.catalog.interfaces.request.UpdateTurmaRequest;
import br.com.escola.catalog.interfaces.request.UpdateTurmaDisciplinaRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/v1")
public class ComandoController {

    private final ComandoUseCase commandUseCase;

    public ComandoController(ComandoUseCase commandUseCase) {
        this.commandUseCase = commandUseCase;
    }

    @PostMapping("/periodos-letivos")
    public ResponseEntity<?> criarPeriodo(
            @Valid @RequestBody CreatePeriodoLetivoRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return created(commandUseCase.criarPeriodo(
                new CreatePeriodoLetivoCommand(
                        request.nome(), request.ano(), request.dataInicio(), request.dataFim()),
                idempotencyKey,
                context));
    }

    @PostMapping("/series")
    public ResponseEntity<?> criarSerie(
            @Valid @RequestBody CreateSerieRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return created(commandUseCase.criarSerie(
                new CreateSerieCommand(request.nome(), request.ordem(), request.nivelEnsinoId()),
                idempotencyKey,
                context));
    }

    @PostMapping("/disciplinas")
    public ResponseEntity<?> criarDisciplina(
            @Valid @RequestBody CreateDisciplinaRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return created(commandUseCase.criarDisciplina(
                new CreateDisciplinaCommand(request.nome(), request.cargaHoraria(), request.ativo()),
                idempotencyKey,
                context));
    }

    @PutMapping("/periodos-letivos/{periodoId}")
    public ResponseEntity<?> atualizarPeriodo(
            @PathVariable UUID periodoId,
            @Valid @RequestBody UpdatePeriodoLetivoRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.atualizarPeriodo(
                periodoId,
                new UpdatePeriodoLetivoCommand(
                        request.nome(), request.ano(), request.dataInicio(), request.dataFim(), request.ativo()),
                idempotencyKey,
                context);
        return ResponseEntity.ok()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @PutMapping("/series/{serieId}")
    public ResponseEntity<?> atualizarSerie(
            @PathVariable UUID serieId,
            @Valid @RequestBody UpdateSerieRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.atualizarSerie(
                serieId,
                new UpdateSerieCommand(request.nome(), request.ordem(), request.nivelEnsinoId()),
                idempotencyKey,
                context);
        return ResponseEntity.ok()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @PutMapping("/disciplinas/{disciplinaId}")
    public ResponseEntity<?> atualizarDisciplina(
            @PathVariable UUID disciplinaId,
            @Valid @RequestBody UpdateDisciplinaRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.atualizarDisciplina(
                disciplinaId,
                new UpdateDisciplinaCommand(request.nome(), request.cargaHoraria(), request.ativo()),
                idempotencyKey,
                context);
        return ResponseEntity.ok()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @PutMapping("/turmas/{turmaId}")
    public ResponseEntity<?> atualizarTurma(
            @PathVariable UUID turmaId,
            @Valid @RequestBody UpdateTurmaRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.atualizarTurma(
                turmaId,
                new UpdateTurmaCommand(
                        request.codigo(), request.nome(), request.capacidade(), request.periodoLetivoId(),
                        request.serieId(), request.turnoId(), request.ativo()),
                idempotencyKey,
                context);
        return ResponseEntity.ok()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @DeleteMapping("/periodos-letivos/{periodoId}")
    public ResponseEntity<Void> excluirPeriodo(
            @PathVariable UUID periodoId,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.excluirPeriodo(periodoId, idempotencyKey, context);
        return ResponseEntity.noContent()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .build();
    }

    @DeleteMapping("/series/{serieId}")
    public ResponseEntity<Void> excluirSerie(
            @PathVariable UUID serieId,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.excluirSerie(serieId, idempotencyKey, context);
        return ResponseEntity.noContent()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .build();
    }

    @DeleteMapping("/disciplinas/{disciplinaId}")
    public ResponseEntity<Void> excluirDisciplina(
            @PathVariable UUID disciplinaId,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.excluirDisciplina(disciplinaId, idempotencyKey, context);
        return ResponseEntity.noContent()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .build();
    }

    @DeleteMapping("/turmas/{turmaId}")
    public ResponseEntity<Void> excluirTurma(
            @PathVariable UUID turmaId,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.excluirTurma(turmaId, idempotencyKey, context);
        return ResponseEntity.noContent()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .build();
    }

    @PostMapping("/turmas")
    public ResponseEntity<?> criarTurma(
            @Valid @RequestBody CreateTurmaRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return created(commandUseCase.criarTurma(
                new CreateTurmaCommand(
                        request.codigo(), request.nome(), request.capacidade(), request.periodoLetivoId(),
                        request.serieId(), request.turnoId()),
                idempotencyKey,
                context));
    }

    @PostMapping("/turmas/{turmaId}/disciplinas")
    public ResponseEntity<?> vincularDisciplina(
            @PathVariable UUID turmaId,
            @Valid @RequestBody LinkDisciplinaRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return created(commandUseCase.vincularDisciplina(
                turmaId,
                new LinkDisciplinaCommand(request.disciplinaId(), request.cargaHoraria()),
                idempotencyKey,
                context));
    }

    @PutMapping("/turmas/{turmaId}/disciplinas/{vinculoId}")
    public ResponseEntity<?> atualizarVinculoDisciplina(
            @PathVariable UUID turmaId,
            @PathVariable UUID vinculoId,
            @Valid @RequestBody UpdateTurmaDisciplinaRequest request,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.atualizarVinculoDisciplina(
                turmaId,
                vinculoId,
                new UpdateTurmaDisciplinaCommand(request.cargaHoraria()),
                idempotencyKey,
                context);
        return ResponseEntity.ok()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @DeleteMapping("/turmas/{turmaId}/disciplinas/{vinculoId}")
    public ResponseEntity<Void> desvincularDisciplina(
            @PathVariable UUID turmaId,
            @PathVariable UUID vinculoId,
            @RequestHeader(InternalHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        var result = commandUseCase.desvincularDisciplina(turmaId, vinculoId, idempotencyKey, context);
        return ResponseEntity.noContent()
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .build();
    }

    private ResponseEntity<?> created(CommandResult<?> result) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .body(result.response());
    }
}

