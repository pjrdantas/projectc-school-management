package br.com.escola.bff.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyDiarioClasseReadPort;
import br.com.escola.bff.application.port.out.DiarioClasseReadPort;
import br.com.escola.bff.application.usecase.ConsultarDiarioClasseUseCase;
import reactor.core.publisher.Mono;

public class DiarioClasseReadProxyService implements ConsultarDiarioClasseUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DiarioClasseReadPort pedagogicalDiarioClasseReadPort;
    private final LegacyDiarioClasseReadPort monolithDiarioClasseReadPort;

    public DiarioClasseReadProxyService(
            InternalAuthContextPort authContextPort,
            DiarioClasseReadPort pedagogicalDiarioClasseReadPort,
            LegacyDiarioClasseReadPort monolithDiarioClasseReadPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalDiarioClasseReadPort = pedagogicalDiarioClasseReadPort;
        this.monolithDiarioClasseReadPort = monolithDiarioClasseReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> carregar(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalDiarioClasseReadPort.carregar(
                        professorId,
                        turmaId,
                        disciplinaId,
                        anoLetivo,
                        mes,
                        dataReferencia,
                        query,
                        context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DiarioClasseReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDiarioClasseReadPort.carregar(
                                professorId,
                                turmaId,
                                disciplinaId,
                                anoLetivo,
                                mes,
                                dataReferencia,
                                query))
                .onErrorResume(DiarioClasseReadFailureException.class,
                        error -> monolithDiarioClasseReadPort.carregar(
                                professorId,
                                turmaId,
                                disciplinaId,
                                anoLetivo,
                                mes,
                                dataReferencia,
                                query));
    }

    private static final class DiarioClasseReadFailureException extends RuntimeException {

        private DiarioClasseReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

