package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurnoResolved;
import reactor.core.publisher.Mono;

public interface CatalogoTurnoResolverPort {

    Mono<TurnoResolved> resolve(CatalogWriteQuery query, AuthSessionContext context, String turno);
}

