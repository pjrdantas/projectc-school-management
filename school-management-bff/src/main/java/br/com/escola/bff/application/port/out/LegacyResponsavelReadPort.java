package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface LegacyResponsavelReadPort {

    Mono<ResponseEntity<String>> listarResponsaveis(
            String nome,
            String cpf,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> buscarResponsavelPorId(
            UUID responsavelId,
            CatalogReadQuery query);
}

