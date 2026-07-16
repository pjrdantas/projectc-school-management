package br.com.escola.bff.application.port.out;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface MonolithDashboardIndicadorHistoricoReadPort {

    Mono<ResponseEntity<String>> consultarHistorico(
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId,
            CatalogReadQuery query);
}
