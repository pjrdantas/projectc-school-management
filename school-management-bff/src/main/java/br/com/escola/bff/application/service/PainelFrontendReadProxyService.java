package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.PainelFrontendReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyPainelFrontendReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelFrontendUseCase;
import reactor.core.publisher.Mono;

public class PainelFrontendReadProxyService implements ConsultarPainelFrontendUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelFrontendReadPort dashboardFrontendReadPort;
    private final LegacyPainelFrontendReadPort monolithPainelFrontendReadPort;

    public PainelFrontendReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelFrontendReadPort dashboardFrontendReadPort,
            LegacyPainelFrontendReadPort monolithPainelFrontendReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardFrontendReadPort = dashboardFrontendReadPort;
        this.monolithPainelFrontendReadPort = monolithPainelFrontendReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(
            String authorization,
            String correlationId,
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardFrontendReadPort.consultar(publicoCodigo, usuarioId, professorId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PainelQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithPainelFrontendReadPort.consultar(publicoCodigo, usuarioId, professorId, query))
                .onErrorResume(PainelQueryReadFailureException.class,
                        error -> monolithPainelFrontendReadPort.consultar(publicoCodigo, usuarioId, professorId, query));
    }

    private static final class PainelQueryReadFailureException extends RuntimeException {

        private PainelQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

