package br.com.escola.bff.application.usecase;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import reactor.core.publisher.Mono;

public interface CreatePeriodoLetivoUseCase {

    Mono<PeriodoLetivoCreatedResult> executar(CatalogWriteQuery query, PeriodoLetivoCreateCommand command);
}
