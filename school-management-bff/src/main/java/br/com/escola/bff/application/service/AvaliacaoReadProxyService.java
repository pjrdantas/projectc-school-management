package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithAvaliacaoReadPort;
import br.com.escola.bff.application.port.out.PedagogicalAvaliacaoPort;
import br.com.escola.bff.application.usecase.ConsultarAvaliacaoUseCase;
import reactor.core.publisher.Mono;

public class AvaliacaoReadProxyService implements ConsultarAvaliacaoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PedagogicalAvaliacaoPort pedagogicalAvaliacaoPort;
    private final MonolithAvaliacaoReadPort monolithAvaliacaoReadPort;

    public AvaliacaoReadProxyService(
            InternalAuthContextPort authContextPort,
            PedagogicalAvaliacaoPort pedagogicalAvaliacaoPort,
            MonolithAvaliacaoReadPort monolithAvaliacaoReadPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalAvaliacaoPort = pedagogicalAvaliacaoPort;
        this.monolithAvaliacaoReadPort = monolithAvaliacaoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listar(
            String authorization,
            String correlationId,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.listar(
                        professorTurmaDisciplinaId,
                        turmaId,
                        query,
                        context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PedagogicalAvaliacaoReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAvaliacaoReadPort.listar(professorTurmaDisciplinaId, turmaId, query))
                .onErrorResume(PedagogicalAvaliacaoReadFailureException.class,
                        error -> monolithAvaliacaoReadPort.listar(professorTurmaDisciplinaId, turmaId, query));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPorId(String authorization, String correlationId, UUID avaliacaoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.buscarPorId(avaliacaoId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PedagogicalAvaliacaoReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAvaliacaoReadPort.buscarPorId(avaliacaoId, query))
                .onErrorResume(PedagogicalAvaliacaoReadFailureException.class,
                        error -> monolithAvaliacaoReadPort.buscarPorId(avaliacaoId, query));
    }

    @Override
    public Mono<ResponseEntity<String>> listarNotasPorAvaliacao(
            String authorization,
            String correlationId,
            UUID avaliacaoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.listarNotasPorAvaliacao(avaliacaoId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PedagogicalAvaliacaoReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAvaliacaoReadPort.listarNotasPorAvaliacao(avaliacaoId, query))
                .onErrorResume(PedagogicalAvaliacaoReadFailureException.class,
                        error -> monolithAvaliacaoReadPort.listarNotasPorAvaliacao(avaliacaoId, query));
    }

    @Override
    public Mono<ResponseEntity<String>> listarNotasPorMatricula(
            String authorization,
            String correlationId,
            UUID matriculaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.listarNotasPorMatricula(matriculaId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                PedagogicalAvaliacaoReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithAvaliacaoReadPort.listarNotasPorMatricula(matriculaId, query))
                .onErrorResume(PedagogicalAvaliacaoReadFailureException.class,
                        error -> monolithAvaliacaoReadPort.listarNotasPorMatricula(matriculaId, query));
    }

    private static final class PedagogicalAvaliacaoReadFailureException extends RuntimeException {

        private PedagogicalAvaliacaoReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
