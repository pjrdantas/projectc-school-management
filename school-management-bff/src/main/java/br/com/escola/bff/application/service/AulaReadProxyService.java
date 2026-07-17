package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyAulaReadPort;
import br.com.escola.bff.application.port.out.AulaPort;
import br.com.escola.bff.application.usecase.ConsultarAulaUseCase;
import reactor.core.publisher.Mono;

public class AulaReadProxyService implements ConsultarAulaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final AulaPort pedagogicalAulaPort;
    private final LegacyAulaReadPort monolithAulaReadPort;

    public AulaReadProxyService(
            InternalAuthContextPort authContextPort,
            AulaPort pedagogicalAulaPort,
            LegacyAulaReadPort monolithAulaReadPort) {
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
                                AulaReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAulaReadPort.listar(professorTurmaDisciplinaId, turmaId, query))
                .onErrorResume(AulaReadFailureException.class,
                        error -> monolithAulaReadPort.listar(professorTurmaDisciplinaId, turmaId, query));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPorId(String authorization, String correlationId, UUID aulaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.buscarPorId(aulaId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                AulaReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAulaReadPort.buscarPorId(aulaId, query))
                .onErrorResume(AulaReadFailureException.class,
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
                                AulaReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAulaReadPort.listarFrequenciaProfessor(aulaId, query))
                .onErrorResume(AulaReadFailureException.class,
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
                                AulaReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAulaReadPort.listarFrequenciasAlunos(aulaId, query))
                .onErrorResume(AulaReadFailureException.class,
                        error -> monolithAulaReadPort.listarFrequenciasAlunos(aulaId, query));
    }

    private static final class AulaReadFailureException extends RuntimeException {

        private AulaReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

