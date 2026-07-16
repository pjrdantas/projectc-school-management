package br.com.escola.bff.application.port.out;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface DashboardIndicadorHistoricoReadPort {

    Mono<ResponseEntity<String>> consultarHistorico(
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId,
            CatalogReadQuery query,
            AuthSessionContext context);
}
