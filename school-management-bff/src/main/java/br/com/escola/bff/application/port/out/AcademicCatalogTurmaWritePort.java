package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import br.com.escola.bff.application.dto.TurnoResolved;
import reactor.core.publisher.Mono;

public interface AcademicCatalogTurmaWritePort {

    Mono<TurmaCreatedResult> criar(
            CatalogWriteQuery query,
            AuthSessionContext context,
            TurnoResolved turno,
            TurmaCreateCommand command);
}
