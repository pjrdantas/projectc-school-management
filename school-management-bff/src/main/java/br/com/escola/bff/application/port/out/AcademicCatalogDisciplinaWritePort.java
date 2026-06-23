package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import reactor.core.publisher.Mono;

public interface AcademicCatalogDisciplinaWritePort {

    Mono<DisciplinaCreatedResult> criar(
            CatalogWriteQuery query,
            AuthSessionContext context,
            DisciplinaCreateCommand command);
}
