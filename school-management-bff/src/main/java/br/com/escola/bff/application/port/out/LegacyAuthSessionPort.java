package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface LegacyAuthSessionPort {

    Mono<ResponseEntity<String>> listarEscolas(CatalogReadQuery query);

    Mono<ResponseEntity<String>> selecionarEscolaAtiva(String requestBody, CatalogReadQuery query);
}

