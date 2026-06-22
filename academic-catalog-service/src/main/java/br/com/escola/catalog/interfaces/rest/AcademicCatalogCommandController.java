package br.com.escola.catalog.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import br.com.escola.catalog.application.context.InternalHeaders;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.port.in.CatalogCommandUseCase;
import br.com.escola.catalog.interfaces.request.CreateDisciplinaRequest;
import br.com.escola.catalog.interfaces.request.CreatePeriodoLetivoRequest;
import br.com.escola.catalog.interfaces.request.CreateSerieRequest;
import br.com.escola.catalog.interfaces.request.CreateTurmaRequest;
import br.com.escola.catalog.interfaces.request.LinkDisciplinaRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/v1")
public class AcademicCatalogCommandController {

    private final CatalogCommandUseCase commandUseCase;

    public AcademicCatalogCommandController(CatalogCommandUseCase commandUseCase) {
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
                new CreateDisciplinaCommand(request.nome(), request.cargaHoraria()),
                idempotencyKey,
                context));
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

    private ResponseEntity<?> created(CommandResult<?> result) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(InternalHeaders.IDEMPOTENCY_REPLAYED, Boolean.toString(result.replayed()))
                .body(result.response());
    }
}
