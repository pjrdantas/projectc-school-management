package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.NivelEnsinoResolved;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import reactor.core.publisher.Mono;

public interface AcademicCatalogSerieWritePort {

    Mono<SerieCreatedResult> criar(
            CatalogWriteQuery query,
            AuthSessionContext context,
            NivelEnsinoResolved nivelEnsino,
            SerieCreateCommand command);
}
