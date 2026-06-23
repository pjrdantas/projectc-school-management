package br.com.escola.bff.application.usecase;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import reactor.core.publisher.Mono;

public interface CreateDisciplinaUseCase {

    Mono<DisciplinaCreatedResult> executar(CatalogWriteQuery query, DisciplinaCreateCommand command);
}
