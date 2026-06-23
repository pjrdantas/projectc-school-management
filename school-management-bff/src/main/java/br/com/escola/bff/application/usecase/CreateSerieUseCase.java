package br.com.escola.bff.application.usecase;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import reactor.core.publisher.Mono;

public interface CreateSerieUseCase {

    Mono<SerieCreatedResult> executar(CatalogWriteQuery query, SerieCreateCommand command);
}
