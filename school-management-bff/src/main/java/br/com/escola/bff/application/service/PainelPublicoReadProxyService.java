package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.PainelPublicoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyPainelPublicoReadPort;
import br.com.escola.bff.application.usecase.ListarPainelPublicoUseCase;
import reactor.core.publisher.Mono;

public class PainelPublicoReadProxyService implements ListarPainelPublicoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelPublicoReadPort dashboardPublicoReadPort;
    private final LegacyPainelPublicoReadPort monolithPainelPublicoReadPort;

    public PainelPublicoReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelPublicoReadPort dashboardPublicoReadPort,
            LegacyPainelPublicoReadPort monolithPainelPublicoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardPublicoReadPort = dashboardPublicoReadPort;
        this.monolithPainelPublicoReadPort = monolithPainelPublicoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarPublicos(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardPublicoReadPort.listarPublicos(query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PainelQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithPainelPublicoReadPort.listarPublicos(query))
                .onErrorResume(PainelQueryReadFailureException.class,
                        error -> monolithPainelPublicoReadPort.listarPublicos(query));
    }

    private static final class PainelQueryReadFailureException extends RuntimeException {

        private PainelQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

