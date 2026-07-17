package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface LegacyPainelPublicoReadPort {

    Mono<ResponseEntity<String>> listarPublicos(CatalogReadQuery query);
}

