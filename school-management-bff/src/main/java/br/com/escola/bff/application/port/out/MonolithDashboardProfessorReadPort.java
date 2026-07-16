package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface MonolithDashboardProfessorReadPort {

    Mono<ResponseEntity<String>> consultar(UUID professorId, CatalogReadQuery query);
}
