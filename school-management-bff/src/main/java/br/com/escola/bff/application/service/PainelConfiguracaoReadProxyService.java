package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.PainelConfiguracaoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyPainelConfiguracaoReadPort;
import br.com.escola.bff.application.usecase.ListarPainelConfiguracaoUseCase;
import reactor.core.publisher.Mono;

public class PainelConfiguracaoReadProxyService implements ListarPainelConfiguracaoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelConfiguracaoReadPort dashboardConfiguracaoReadPort;
    private final LegacyPainelConfiguracaoReadPort monolithPainelConfiguracaoReadPort;

    public PainelConfiguracaoReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelConfiguracaoReadPort dashboardConfiguracaoReadPort,
            LegacyPainelConfiguracaoReadPort monolithPainelConfiguracaoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardConfiguracaoReadPort = dashboardConfiguracaoReadPort;
        this.monolithPainelConfiguracaoReadPort = monolithPainelConfiguracaoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarPainels(
            String authorization,
            String correlationId,
            UUID publicoPainelId,
            String publicoCodigo) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardConfiguracaoReadPort
                        .listarPainels(publicoPainelId, publicoCodigo, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PainelQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithPainelConfiguracaoReadPort
                                .listarPainels(publicoPainelId, publicoCodigo, query))
                .onErrorResume(PainelQueryReadFailureException.class,
                        error -> monolithPainelConfiguracaoReadPort
                                .listarPainels(publicoPainelId, publicoCodigo, query));
    }

    private static final class PainelQueryReadFailureException extends RuntimeException {

        private PainelQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

