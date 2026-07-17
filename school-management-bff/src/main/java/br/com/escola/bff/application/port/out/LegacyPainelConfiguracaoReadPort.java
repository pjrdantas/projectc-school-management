package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface LegacyPainelConfiguracaoReadPort {

    Mono<ResponseEntity<String>> listarPainels(
            UUID publicoPainelId,
            String publicoCodigo,
            CatalogReadQuery query);
}

