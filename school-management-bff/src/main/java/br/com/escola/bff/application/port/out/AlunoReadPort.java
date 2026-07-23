package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface AlunoReadPort {

    Mono<ResponseEntity<String>> listar(String nome, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarPorId(UUID alunoId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarFicha(UUID alunoId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> encaminharEscrita(
            HttpMethod method, UUID alunoId, String body, CatalogReadQuery query, AuthSessionContext context);
}
