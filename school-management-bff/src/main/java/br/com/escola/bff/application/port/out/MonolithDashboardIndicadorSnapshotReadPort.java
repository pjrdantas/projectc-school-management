package br.com.escola.bff.application.port.out;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface MonolithDashboardIndicadorSnapshotReadPort {

    Mono<ResponseEntity<String>> listarPorPublicoCodigo(
            String publicoCodigo,
            LocalDate referenciaData,
            CatalogReadQuery query);
}
