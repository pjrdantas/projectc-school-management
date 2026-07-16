package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface DashboardAlertaReadPort {

    Mono<ResponseEntity<String>> consultar(String publicoCodigo, UUID professorId, CatalogReadQuery query, AuthSessionContext context);
}
