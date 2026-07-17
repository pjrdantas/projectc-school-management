package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface LegacyPainelSecretariaReadPort {

    Mono<ResponseEntity<String>> consultar(CatalogReadQuery query);
}

