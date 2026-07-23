package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface FuncionarioCadastroReadPort {

    Mono<ResponseEntity<String>> listarFuncionarios(CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarFuncionarioPorId(UUID funcionarioId, CatalogReadQuery query, AuthSessionContext context);
}

