package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface AuthContextPort {

    Mono<AuthSessionContext> resolve(CatalogReadQuery query);
}
