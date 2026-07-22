package br.com.escola.catalog.application.port.in;

import java.util.UUID;

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
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;

public interface ComandoUseCase {

    CommandResult<PeriodoLetivoResponse> criarPeriodo(
            CreatePeriodoLetivoCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<PeriodoLetivoResponse> atualizarPeriodo(
            UUID periodoId, UpdatePeriodoLetivoCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<Void> excluirPeriodo(UUID periodoId, String idempotencyKey, InternalRequestContext context);

    CommandResult<SerieResponse> criarSerie(
            CreateSerieCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<SerieResponse> atualizarSerie(
            UUID serieId, UpdateSerieCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<Void> excluirSerie(UUID serieId, String idempotencyKey, InternalRequestContext context);

    CommandResult<DisciplinaResponse> criarDisciplina(
            CreateDisciplinaCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<DisciplinaResponse> atualizarDisciplina(
            UUID disciplinaId, UpdateDisciplinaCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<Void> excluirDisciplina(UUID disciplinaId, String idempotencyKey, InternalRequestContext context);

    CommandResult<TurmaResponse> criarTurma(
            CreateTurmaCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<TurmaResponse> atualizarTurma(
            UUID turmaId, UpdateTurmaCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<Void> excluirTurma(UUID turmaId, String idempotencyKey, InternalRequestContext context);

    CommandResult<TurmaDisciplinaResponse> vincularDisciplina(
            UUID turmaId,
            LinkDisciplinaCommand command,
            String idempotencyKey,
            InternalRequestContext context);

    CommandResult<TurmaDisciplinaResponse> atualizarVinculoDisciplina(
            UUID turmaId,
            UUID vinculoId,
            UpdateTurmaDisciplinaCommand command,
            String idempotencyKey,
            InternalRequestContext context);

    CommandResult<Void> desvincularDisciplina(
            UUID turmaId,
            UUID vinculoId,
            String idempotencyKey,
            InternalRequestContext context);
}

