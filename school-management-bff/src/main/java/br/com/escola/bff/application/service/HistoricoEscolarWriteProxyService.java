package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PedagogicalHistoricoEscolarWritePort;
import br.com.escola.bff.application.usecase.AtualizarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.CriarHistoricoEscolarUseCase;
import reactor.core.publisher.Mono;

public class HistoricoEscolarWriteProxyService implements CriarHistoricoEscolarUseCase, AtualizarHistoricoEscolarUseCase {

    private final AuthContextPort authContextPort;
    private final PedagogicalHistoricoEscolarWritePort pedagogicalHistoricoEscolarWritePort;

    public HistoricoEscolarWriteProxyService(
            AuthContextPort authContextPort,
            PedagogicalHistoricoEscolarWritePort pedagogicalHistoricoEscolarWritePort) {
        this.authContextPort = authContextPort;
        this.pedagogicalHistoricoEscolarWritePort = pedagogicalHistoricoEscolarWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalHistoricoEscolarWritePort.criar(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            UUID historicoEscolarId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalHistoricoEscolarWritePort.atualizar(historicoEscolarId, requestBody, query, context));
    }
}
