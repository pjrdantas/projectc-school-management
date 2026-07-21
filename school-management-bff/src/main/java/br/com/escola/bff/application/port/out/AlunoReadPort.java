package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface AlunoReadPort {

    Mono<ResponseEntity<String>> listar(String nome, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarPorId(UUID alunoId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarFicha(UUID alunoId, CatalogReadQuery query, AuthSessionContext context);
}
