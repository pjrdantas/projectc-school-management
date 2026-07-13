package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface IdentityAccessSessionPort {

    Mono<ResponseEntity<String>> listarEscolas(CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> selecionarEscolaAtiva(
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);
}
