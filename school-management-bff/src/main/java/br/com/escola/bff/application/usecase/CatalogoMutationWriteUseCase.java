package br.com.escola.bff.application.usecase;

import java.util.UUID;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import br.com.escola.bff.application.dto.DisciplinaUpdateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import br.com.escola.bff.application.dto.PeriodoLetivoUpdateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import br.com.escola.bff.application.dto.SerieUpdateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkedResult;
import br.com.escola.bff.application.dto.TurmaDisciplinaUpdateCommand;
import br.com.escola.bff.application.dto.TurmaUpdateCommand;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;

public interface CatalogoMutationWriteUseCase {

    Mono<JsonNode> criarTurno(CatalogWriteQuery query, String codigo, String descricao);

    Mono<JsonNode> atualizarTurno(UUID turnoId, CatalogWriteQuery query, String codigo, String descricao);

    Mono<PeriodoLetivoCreatedResult> atualizarPeriodo(UUID periodoId, CatalogWriteQuery query, PeriodoLetivoUpdateCommand command);

    Mono<Void> excluirPeriodo(UUID periodoId, CatalogWriteQuery query);

    Mono<DisciplinaCreatedResult> atualizarDisciplina(UUID disciplinaId, CatalogWriteQuery query, DisciplinaUpdateCommand command);

    Mono<Void> excluirDisciplina(UUID disciplinaId, CatalogWriteQuery query);

    Mono<SerieCreatedResult> atualizarSerie(UUID serieId, CatalogWriteQuery query, SerieUpdateCommand command);

    Mono<Void> excluirSerie(UUID serieId, CatalogWriteQuery query);

    Mono<TurmaCreatedResult> atualizarTurma(UUID turmaId, CatalogWriteQuery query, TurmaUpdateCommand command);

    Mono<Void> excluirTurma(UUID turmaId, CatalogWriteQuery query);

    Mono<TurmaDisciplinaLinkedResult> atualizarVinculo(
            UUID turmaId, UUID vinculoId, CatalogWriteQuery query, TurmaDisciplinaUpdateCommand command);

    Mono<Void> desvincular(UUID turmaId, UUID vinculoId, CatalogWriteQuery query);
}
