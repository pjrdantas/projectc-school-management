package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithResponsavelReadPort;
import br.com.escola.bff.application.port.out.ResponsiblesReadPort;
import br.com.escola.bff.application.usecase.ConsultarResponsavelUseCase;
import reactor.core.publisher.Mono;

public class ResponsavelReadProxyService implements ConsultarResponsavelUseCase {

    private final InternalAuthContextPort authContextPort;
    private final ResponsiblesReadPort responsiblesReadPort;
    private final MonolithResponsavelReadPort monolithResponsavelReadPort;

    public ResponsavelReadProxyService(
            InternalAuthContextPort authContextPort,
            ResponsiblesReadPort responsiblesReadPort,
            MonolithResponsavelReadPort monolithResponsavelReadPort) {
        this.authContextPort = authContextPort;
        this.responsiblesReadPort = responsiblesReadPort;
        this.monolithResponsavelReadPort = monolithResponsavelReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarResponsaveis(
            String authorization,
            String correlationId,
            String nome,
            String cpf) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> responsiblesReadPort.listarResponsaveis(nome, cpf, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                ResponsiblesReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithResponsavelReadPort.listarResponsaveis(nome, cpf, query))
                .onErrorResume(ResponsiblesReadFailureException.class,
                        error -> monolithResponsavelReadPort.listarResponsaveis(nome, cpf, query));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarResponsavelPorId(
            String authorization,
            String correlationId,
            UUID responsavelId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> responsiblesReadPort.buscarResponsavelPorId(responsavelId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                ResponsiblesReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithResponsavelReadPort.buscarResponsavelPorId(responsavelId, query))
                .onErrorResume(ResponsiblesReadFailureException.class,
                        error -> monolithResponsavelReadPort.buscarResponsavelPorId(responsavelId, query));
    }

    private static final class ResponsiblesReadFailureException extends RuntimeException {

        private ResponsiblesReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
