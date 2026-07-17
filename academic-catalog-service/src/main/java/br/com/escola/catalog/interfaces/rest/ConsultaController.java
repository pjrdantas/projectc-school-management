package br.com.escola.catalog.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.catalog.application.context.InternalHeaders;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.NivelEnsinoResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;
import br.com.escola.catalog.application.dto.TurnoResponse;
import br.com.escola.catalog.application.port.in.ConsultaUseCase;

@RestController
@RequestMapping("/internal/v1")
public class ConsultaController {

    private final ConsultaUseCase catalogQueryUseCase;

    public ConsultaController(ConsultaUseCase catalogQueryUseCase) {
        this.catalogQueryUseCase = catalogQueryUseCase;
    }

    @GetMapping("/catalogos/niveis-ensino")
    public List<NivelEnsinoResponse> listarNiveisEnsino(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.listarNiveisEnsino(context);
    }

    @GetMapping("/turnos")
    public List<TurnoResponse> listarTurnos(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.listarTurnos(context);
    }

    @GetMapping("/turnos/{id}")
    public TurnoResponse buscarTurno(
            @PathVariable UUID id,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.buscarTurno(id, context);
    }

    @GetMapping("/periodos-letivos")
    public List<PeriodoLetivoResponse> listarPeriodos(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.listarPeriodos(context);
    }

    @GetMapping("/periodos-letivos/{id}")
    public PeriodoLetivoResponse buscarPeriodo(
            @PathVariable UUID id,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.buscarPeriodo(id, context);
    }

    @GetMapping("/series")
    public List<SerieResponse> listarSeries(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.listarSeries(context);
    }

    @GetMapping("/series/{id}")
    public SerieResponse buscarSerie(
            @PathVariable UUID id,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.buscarSerie(id, context);
    }

    @GetMapping("/turmas")
    public List<TurmaResponse> listarTurmas(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.listarTurmas(context);
    }

    @GetMapping("/turmas/{id}")
    public TurmaResponse buscarTurma(
            @PathVariable UUID id,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.buscarTurma(id, context);
    }

    @GetMapping("/disciplinas")
    public List<DisciplinaResponse> listarDisciplinas(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.listarDisciplinas(context);
    }

    @GetMapping("/disciplinas/{id}")
    public DisciplinaResponse buscarDisciplina(
            @PathVariable UUID id,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.buscarDisciplina(id, context);
    }

    @GetMapping("/turmas/{turmaId}/disciplinas")
    public List<TurmaDisciplinaResponse> listarDisciplinasDaTurma(
            @PathVariable UUID turmaId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return catalogQueryUseCase.listarDisciplinasDaTurma(turmaId, context);
    }
}

