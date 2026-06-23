package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import reactor.core.publisher.Mono;

public interface MonolithDisciplinaWritePort {

    Mono<DisciplinaCreatedResult> criar(CatalogWriteQuery query, DisciplinaCreateCommand command);
}
