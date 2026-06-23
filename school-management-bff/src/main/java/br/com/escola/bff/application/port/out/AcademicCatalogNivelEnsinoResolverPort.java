package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.NivelEnsinoResolved;
import reactor.core.publisher.Mono;

public interface AcademicCatalogNivelEnsinoResolverPort {

    Mono<NivelEnsinoResolved> resolve(CatalogWriteQuery query, AuthSessionContext context, String nivelEnsino);
}
