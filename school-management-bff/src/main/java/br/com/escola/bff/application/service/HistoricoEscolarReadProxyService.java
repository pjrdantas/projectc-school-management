package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.LegacyHistoricoEscolarReadPort;
import br.com.escola.bff.application.port.out.HistoricoEscolarReadPort;
import br.com.escola.bff.application.usecase.ConsultarHistoricoEscolarUseCase;
import reactor.core.publisher.Mono;

public class HistoricoEscolarReadProxyService implements ConsultarHistoricoEscolarUseCase {

    private final InternalAuthContextPort authContextPort;
    private final HistoricoEscolarReadPort pedagogicalHistoricoEscolarReadPort;
    private final LegacyHistoricoEscolarReadPort monolithHistoricoEscolarReadPort;

    public HistoricoEscolarReadProxyService(
            InternalAuthContextPort authContextPort,
            HistoricoEscolarReadPort pedagogicalHistoricoEscolarReadPort,
            LegacyHistoricoEscolarReadPort monolithHistoricoEscolarReadPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalHistoricoEscolarReadPort = pedagogicalHistoricoEscolarReadPort;
        this.monolithHistoricoEscolarReadPort = monolithHistoricoEscolarReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> carregarNovo(
            String authorization,
            String correlationId,
            UUID alunoId,
            UUID matriculaId,
            String modo) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalHistoricoEscolarReadPort.carregarNovo(alunoId, matriculaId, modo, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                HistoricoEscolarReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithHistoricoEscolarReadPort.carregarNovo(alunoId, matriculaId, modo, query))
                .onErrorResume(HistoricoEscolarReadFailureException.class,
                        error -> monolithHistoricoEscolarReadPort.carregarNovo(alunoId, matriculaId, modo, query));
    }

    @Override
    public Mono<ResponseEntity<String>> carregarParaEdicao(
            String authorization,
            String correlationId,
            UUID historicoEscolarId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalHistoricoEscolarReadPort.carregarParaEdicao(historicoEscolarId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                HistoricoEscolarReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithHistoricoEscolarReadPort.carregarParaEdicao(historicoEscolarId, query))
                .onErrorResume(HistoricoEscolarReadFailureException.class,
                        error -> monolithHistoricoEscolarReadPort.carregarParaEdicao(historicoEscolarId, query));
    }

    private static final class HistoricoEscolarReadFailureException extends RuntimeException {

        private HistoricoEscolarReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}

