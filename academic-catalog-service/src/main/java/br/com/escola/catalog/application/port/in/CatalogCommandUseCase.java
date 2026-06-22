package br.com.escola.catalog.application.port.in;

import java.util.UUID;

import br.com.escola.catalog.application.command.CommandResult;
import br.com.escola.catalog.application.command.CreateDisciplinaCommand;
import br.com.escola.catalog.application.command.CreatePeriodoLetivoCommand;
import br.com.escola.catalog.application.command.CreateSerieCommand;
import br.com.escola.catalog.application.command.CreateTurmaCommand;
import br.com.escola.catalog.application.command.LinkDisciplinaCommand;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;

public interface CatalogCommandUseCase {

    CommandResult<PeriodoLetivoResponse> criarPeriodo(
            CreatePeriodoLetivoCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<SerieResponse> criarSerie(
            CreateSerieCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<DisciplinaResponse> criarDisciplina(
            CreateDisciplinaCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<TurmaResponse> criarTurma(
            CreateTurmaCommand command, String idempotencyKey, InternalRequestContext context);

    CommandResult<TurmaDisciplinaResponse> vincularDisciplina(
            UUID turmaId,
            LinkDisciplinaCommand command,
            String idempotencyKey,
            InternalRequestContext context);
}
