package br.com.escola.bff.application.port.out;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PessoaCatalogoReadPort {

    Mono<ResponseEntity<String>> listarTiposPessoa(CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> listarTiposEndereco(CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> listarStatusAluno(CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> listarParentescos(CatalogReadQuery query, AuthSessionContext context);
}

