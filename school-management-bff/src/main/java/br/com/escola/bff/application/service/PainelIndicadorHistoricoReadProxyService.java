package br.com.escola.bff.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelIndicadorHistoricoReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelIndicadorHistoricoUseCase;
import reactor.core.publisher.Mono;

public class PainelIndicadorHistoricoReadProxyService implements ConsultarPainelIndicadorHistoricoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelIndicadorHistoricoReadPort dashboardIndicadorHistoricoReadPort;

    public PainelIndicadorHistoricoReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelIndicadorHistoricoReadPort dashboardIndicadorHistoricoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardIndicadorHistoricoReadPort = dashboardIndicadorHistoricoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultarHistorico(
            String authorization,
            String correlationId,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardIndicadorHistoricoReadPort
                        .consultarHistorico(publicoCodigo, codigoIndicador, dataInicio, dataFim, professorId, query, context));
    }
}
