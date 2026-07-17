package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.PainelAcademicoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyPainelAcademicoReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelAcademicoUseCase;
import reactor.core.publisher.Mono;

public class PainelAcademicoReadProxyService implements ConsultarPainelAcademicoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelAcademicoReadPort dashboardAcademicoReadPort;
    private final LegacyPainelAcademicoReadPort monolithPainelAcademicoReadPort;

    public PainelAcademicoReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelAcademicoReadPort dashboardAcademicoReadPort,
            LegacyPainelAcademicoReadPort monolithPainelAcademicoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardAcademicoReadPort = dashboardAcademicoReadPort;
        this.monolithPainelAcademicoReadPort = monolithPainelAcademicoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardAcademicoReadPort.consultar(query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PainelQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithPainelAcademicoReadPort.consultar(query))
                .onErrorResume(PainelQueryReadFailureException.class,
                        error -> monolithPainelAcademicoReadPort.consultar(query));
    }

    private static final class PainelQueryReadFailureException extends RuntimeException {

        private PainelQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

