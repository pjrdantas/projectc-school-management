package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface ResponsavelCatalogoReadPort {

    Mono<ResponseEntity<String>> listarResponsaveis(
            String nome,
            String cpf,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarResponsavelPorId(
            UUID responsavelId,
            CatalogReadQuery query,
            AuthSessionContext context);
}

