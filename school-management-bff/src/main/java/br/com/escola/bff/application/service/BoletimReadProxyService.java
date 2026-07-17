package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyBoletimReadPort;
import br.com.escola.bff.application.port.out.BoletimReadPort;
import br.com.escola.bff.application.usecase.ConsultarBoletimUseCase;
import reactor.core.publisher.Mono;

public class BoletimReadProxyService implements ConsultarBoletimUseCase {

    private final InternalAuthContextPort authContextPort;
    private final BoletimReadPort pedagogicalBoletimReadPort;
    private final LegacyBoletimReadPort monolithBoletimReadPort;

    public BoletimReadProxyService(
            InternalAuthContextPort authContextPort,
            BoletimReadPort pedagogicalBoletimReadPort,
            LegacyBoletimReadPort monolithBoletimReadPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalBoletimReadPort = pedagogicalBoletimReadPort;
        this.monolithBoletimReadPort = monolithBoletimReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultarBoletimPorMatricula(
            String authorization,
            String correlationId,
            UUID matriculaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalBoletimReadPort.consultarBoletimPorMatricula(
                        matriculaId,
                        query,
                        context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                BoletimReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithBoletimReadPort.consultarBoletimPorMatricula(matriculaId, query))
                .onErrorResume(BoletimReadFailureException.class,
                        error -> monolithBoletimReadPort.consultarBoletimPorMatricula(matriculaId, query));
    }

    @Override
    public Mono<ResponseEntity<String>> listarFechamentosPorMatricula(
            String authorization,
            String correlationId,
            UUID matriculaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalBoletimReadPort.listarFechamentosPorMatricula(
                        matriculaId,
                        query,
                        context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                BoletimReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithBoletimReadPort.listarFechamentosPorMatricula(matriculaId, query))
                .onErrorResume(BoletimReadFailureException.class,
                        error -> monolithBoletimReadPort.listarFechamentosPorMatricula(matriculaId, query));
    }

    private static final class BoletimReadFailureException extends RuntimeException {

        private BoletimReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

