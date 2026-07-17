package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithAlunoResponsavelReadPort;
import br.com.escola.bff.application.port.out.ResponsiblesAlunoResponsavelReadPort;
import br.com.escola.bff.application.usecase.ConsultarAlunoResponsavelUseCase;
import reactor.core.publisher.Mono;

public class AlunoResponsavelReadProxyService implements ConsultarAlunoResponsavelUseCase {

    private final InternalAuthContextPort authContextPort;
    private final ResponsiblesAlunoResponsavelReadPort responsiblesAlunoResponsavelReadPort;
    private final MonolithAlunoResponsavelReadPort monolithAlunoResponsavelReadPort;

    public AlunoResponsavelReadProxyService(
            InternalAuthContextPort authContextPort,
            ResponsiblesAlunoResponsavelReadPort responsiblesAlunoResponsavelReadPort,
            MonolithAlunoResponsavelReadPort monolithAlunoResponsavelReadPort) {
        this.authContextPort = authContextPort;
        this.responsiblesAlunoResponsavelReadPort = responsiblesAlunoResponsavelReadPort;
        this.monolithAlunoResponsavelReadPort = monolithAlunoResponsavelReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarResponsaveisPorAluno(
            String authorization,
            String correlationId,
            UUID alunoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> responsiblesAlunoResponsavelReadPort.listarResponsaveisPorAluno(alunoId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                ResponsiblesAlunoReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAlunoResponsavelReadPort.listarResponsaveisPorAluno(alunoId, query))
                .onErrorResume(ResponsiblesAlunoReadFailureException.class,
                        error -> monolithAlunoResponsavelReadPort.listarResponsaveisPorAluno(alunoId, query));
    }

    private static final class ResponsiblesAlunoReadFailureException extends RuntimeException {

        private ResponsiblesAlunoReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
