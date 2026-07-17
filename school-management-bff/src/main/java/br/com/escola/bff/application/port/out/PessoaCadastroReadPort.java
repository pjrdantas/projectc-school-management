package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PessoaCadastroReadPort {

    Mono<ResponseEntity<String>> buscarPessoaPorId(UUID pessoaId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarEnderecoPrincipal(UUID pessoaId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> listarEnderecos(UUID pessoaId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarContato(UUID pessoaId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> listarDocumentos(UUID pessoaId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarDocumentoPorId(UUID documentoId, CatalogReadQuery query, AuthSessionContext context);
}

