package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.PainelSecretariaReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyPainelSecretariaReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelSecretariaUseCase;
import reactor.core.publisher.Mono;

public class PainelSecretariaReadProxyService implements ConsultarPainelSecretariaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelSecretariaReadPort dashboardSecretariaReadPort;
    private final LegacyPainelSecretariaReadPort monolithPainelSecretariaReadPort;

    public PainelSecretariaReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelSecretariaReadPort dashboardSecretariaReadPort,
            LegacyPainelSecretariaReadPort monolithPainelSecretariaReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardSecretariaReadPort = dashboardSecretariaReadPort;
        this.monolithPainelSecretariaReadPort = monolithPainelSecretariaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardSecretariaReadPort.consultar(query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PainelQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithPainelSecretariaReadPort.consultar(query))
                .onErrorResume(PainelQueryReadFailureException.class,
                        error -> monolithPainelSecretariaReadPort.consultar(query));
    }

    private static final class PainelQueryReadFailureException extends RuntimeException {

        private PainelQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

