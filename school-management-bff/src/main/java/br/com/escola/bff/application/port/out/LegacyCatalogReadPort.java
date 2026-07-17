package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface LegacyCatalogReadPort {

    Mono<ResponseEntity<String>> get(String path, CatalogReadQuery query);
}

