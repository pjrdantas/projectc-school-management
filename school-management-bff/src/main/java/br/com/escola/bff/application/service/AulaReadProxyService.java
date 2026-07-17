package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithAulaReadPort;
import br.com.escola.bff.application.port.out.PedagogicalAulaPort;
import br.com.escola.bff.application.usecase.ConsultarAulaUseCase;
import reactor.core.publisher.Mono;

public class AulaReadProxyService implements ConsultarAulaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PedagogicalAulaPort pedagogicalAulaPort;
    private final MonolithAulaReadPort monolithAulaReadPort;

    public AulaReadProxyService(
            InternalAuthContextPort authContextPort,
            PedagogicalAulaPort pedagogicalAulaPort,
            MonolithAulaReadPort monolithAulaReadPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalAulaPort = pedagogicalAulaPort;
        this.monolithAulaReadPort = monolithAulaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listar(
            String authorization,
            String correlationId,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.listar(
                        professorTurmaDisciplinaId,
                        turmaId,
                        query,
                        context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PedagogicalAulaReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAulaReadPort.listar(professorTurmaDisciplinaId, turmaId, query))
                .onErrorResume(PedagogicalAulaReadFailureException.class,
                        error -> monolithAulaReadPort.listar(professorTurmaDisciplinaId, turmaId, query));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPorId(String authorization, String correlationId, UUID aulaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.buscarPorId(aulaId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PedagogicalAulaReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAulaReadPort.buscarPorId(aulaId, query))
                .onErrorResume(PedagogicalAulaReadFailureException.class,
                        error -> monolithAulaReadPort.buscarPorId(aulaId, query));
    }

    @Override
    public Mono<ResponseEntity<String>> listarFrequenciaProfessor(
            String authorization,
            String correlationId,
            UUID aulaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.listarFrequenciaProfessor(aulaId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PedagogicalAulaReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAulaReadPort.listarFrequenciaProfessor(aulaId, query))
                .onErrorResume(PedagogicalAulaReadFailureException.class,
                        error -> monolithAulaReadPort.listarFrequenciaProfessor(aulaId, query));
    }

    @Override
    public Mono<ResponseEntity<String>> listarFrequenciasAlunos(
            String authorization,
            String correlationId,
            UUID aulaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.listarFrequenciasAlunos(aulaId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PedagogicalAulaReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAulaReadPort.listarFrequenciasAlunos(aulaId, query))
                .onErrorResume(PedagogicalAulaReadFailureException.class,
                        error -> monolithAulaReadPort.listarFrequenciasAlunos(aulaId, query));
    }

    private static final class PedagogicalAulaReadFailureException extends RuntimeException {

        private PedagogicalAulaReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
