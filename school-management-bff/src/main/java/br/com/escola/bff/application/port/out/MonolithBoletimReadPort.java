package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface MonolithBoletimReadPort {

    Mono<ResponseEntity<String>> consultarBoletimPorMatricula(
            UUID matriculaId,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> listarFechamentosPorMatricula(
            UUID matriculaId,
            CatalogReadQuery query);
}
