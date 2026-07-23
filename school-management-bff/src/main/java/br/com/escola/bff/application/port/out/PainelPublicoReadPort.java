package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PainelPublicoReadPort {

    Mono<ResponseEntity<String>> listarPublicos(
            CatalogReadQuery query,
            AuthSessionContext context);
}

