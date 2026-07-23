package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PainelUsuarioPreferenciaReadPort {

    Mono<ResponseEntity<String>> listar(
            UUID usuarioId, UUID painelId, CatalogReadQuery query, AuthSessionContext context);
}
