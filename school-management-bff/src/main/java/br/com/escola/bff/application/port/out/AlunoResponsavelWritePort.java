package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface AlunoResponsavelWritePort {

    Mono<ResponseEntity<String>> vincular(
            UUID alunoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> desvincular(
            UUID alunoId,
            UUID responsavelId,
            CatalogReadQuery query,
            AuthSessionContext context);
}
