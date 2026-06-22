package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.service.CatalogReadRoute;
import reactor.core.publisher.Mono;

public interface RouteCatalogReadUseCase {

    Mono<ResponseEntity<String>> executar(CatalogReadRoute route, CatalogReadQuery query, String... pathArgs);
}
