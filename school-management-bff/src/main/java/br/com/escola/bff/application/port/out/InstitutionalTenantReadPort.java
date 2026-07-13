package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface InstitutionalTenantReadPort {

    Mono<ResponseEntity<String>> consultarTenantAtivo(CatalogReadQuery query, AuthSessionContext context);
}
