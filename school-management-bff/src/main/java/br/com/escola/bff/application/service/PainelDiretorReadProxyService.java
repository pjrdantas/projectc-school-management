package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.PainelDiretorReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyPainelDiretorReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelDiretorUseCase;
import reactor.core.publisher.Mono;

public class PainelDiretorReadProxyService implements ConsultarPainelDiretorUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelDiretorReadPort dashboardDiretorReadPort;
    private final LegacyPainelDiretorReadPort monolithPainelDiretorReadPort;

    public PainelDiretorReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelDiretorReadPort dashboardDiretorReadPort,
            LegacyPainelDiretorReadPort monolithPainelDiretorReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardDiretorReadPort = dashboardDiretorReadPort;
        this.monolithPainelDiretorReadPort = monolithPainelDiretorReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardDiretorReadPort.consultar(query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PainelQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithPainelDiretorReadPort.consultar(query))
                .onErrorResume(PainelQueryReadFailureException.class,
                        error -> monolithPainelDiretorReadPort.consultar(query));
    }

    private static final class PainelQueryReadFailureException extends RuntimeException {

        private PainelQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

