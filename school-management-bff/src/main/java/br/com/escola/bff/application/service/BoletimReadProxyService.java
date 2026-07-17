package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithBoletimReadPort;
import br.com.escola.bff.application.port.out.PedagogicalBoletimReadPort;
import br.com.escola.bff.application.usecase.ConsultarBoletimUseCase;
import reactor.core.publisher.Mono;

public class BoletimReadProxyService implements ConsultarBoletimUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PedagogicalBoletimReadPort pedagogicalBoletimReadPort;
    private final MonolithBoletimReadPort monolithBoletimReadPort;

    public BoletimReadProxyService(
            InternalAuthContextPort authContextPort,
            PedagogicalBoletimReadPort pedagogicalBoletimReadPort,
            MonolithBoletimReadPort monolithBoletimReadPort) {
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
                                PedagogicalBoletimReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithBoletimReadPort.consultarBoletimPorMatricula(matriculaId, query))
                .onErrorResume(PedagogicalBoletimReadFailureException.class,
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
                                PedagogicalBoletimReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithBoletimReadPort.listarFechamentosPorMatricula(matriculaId, query))
                .onErrorResume(PedagogicalBoletimReadFailureException.class,
                        error -> monolithBoletimReadPort.listarFechamentosPorMatricula(matriculaId, query));
    }

    private static final class PedagogicalBoletimReadFailureException extends RuntimeException {

        private PedagogicalBoletimReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
