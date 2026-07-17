package br.com.escola.bff.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.PainelIndicadorHistoricoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyPainelIndicadorHistoricoReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelIndicadorHistoricoUseCase;
import reactor.core.publisher.Mono;

public class PainelIndicadorHistoricoReadProxyService implements ConsultarPainelIndicadorHistoricoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelIndicadorHistoricoReadPort dashboardIndicadorHistoricoReadPort;
    private final LegacyPainelIndicadorHistoricoReadPort monolithPainelIndicadorHistoricoReadPort;

    public PainelIndicadorHistoricoReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelIndicadorHistoricoReadPort dashboardIndicadorHistoricoReadPort,
            LegacyPainelIndicadorHistoricoReadPort monolithPainelIndicadorHistoricoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardIndicadorHistoricoReadPort = dashboardIndicadorHistoricoReadPort;
        this.monolithPainelIndicadorHistoricoReadPort = monolithPainelIndicadorHistoricoReadPort;
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
                        .consultarHistorico(publicoCodigo, codigoIndicador, dataInicio, dataFim, professorId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PainelQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithPainelIndicadorHistoricoReadPort
                                .consultarHistorico(publicoCodigo, codigoIndicador, dataInicio, dataFim, professorId, query))
                .onErrorResume(PainelQueryReadFailureException.class,
                        error -> monolithPainelIndicadorHistoricoReadPort
                                .consultarHistorico(publicoCodigo, codigoIndicador, dataInicio, dataFim, professorId, query));
    }

    private static final class PainelQueryReadFailureException extends RuntimeException {

        private PainelQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

