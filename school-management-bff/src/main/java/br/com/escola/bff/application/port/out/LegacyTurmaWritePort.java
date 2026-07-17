package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import reactor.core.publisher.Mono;

public interface LegacyTurmaWritePort {

    Mono<TurmaCreatedResult> criar(CatalogWriteQuery query, TurmaCreateCommand command);
}

