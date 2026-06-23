package br.com.escola.bff.application.usecase;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import reactor.core.publisher.Mono;

public interface CreateTurmaUseCase {

    Mono<TurmaCreatedResult> executar(CatalogWriteQuery query, TurmaCreateCommand command);
}
