package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface DashboardConfiguracaoReadPort {

    Mono<ResponseEntity<String>> listarDashboards(
            UUID publicoDashboardId,
            String publicoCodigo,
            CatalogReadQuery query,
            AuthSessionContext context);
}
