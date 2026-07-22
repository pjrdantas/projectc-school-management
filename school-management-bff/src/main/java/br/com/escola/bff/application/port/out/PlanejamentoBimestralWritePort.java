package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PlanejamentoBimestralWritePort {

    Mono<ResponseEntity<String>> criar(String requestBody, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> atualizar(
            UUID planejamentoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> alterarStatus(
            UUID planejamentoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> adicionarAula(UUID planejamentoId, String requestBody, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> adicionarAvaliacao(UUID planejamentoId, String requestBody, CatalogReadQuery query, AuthSessionContext context);
}
