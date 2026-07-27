package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.HistoricoEscolarWritePort;
import br.com.escola.bff.application.usecase.AtualizarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.CriarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.ExcluirHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.ImportarHistoricoEscolarPdfUseCase;
import reactor.core.publisher.Mono;

public class HistoricoEscolarWriteProxyService implements CriarHistoricoEscolarUseCase, AtualizarHistoricoEscolarUseCase, ExcluirHistoricoEscolarUseCase, ImportarHistoricoEscolarPdfUseCase {

    private final AuthContextPort authContextPort;
    private final HistoricoEscolarWritePort pedagogicalHistoricoEscolarWritePort;

    public HistoricoEscolarWriteProxyService(
            AuthContextPort authContextPort,
            HistoricoEscolarWritePort pedagogicalHistoricoEscolarWritePort) {
        this.authContextPort = authContextPort;
        this.pedagogicalHistoricoEscolarWritePort = pedagogicalHistoricoEscolarWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(String authorization, String correlationId, FilePart arquivo) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalHistoricoEscolarWritePort.importarPdf(arquivo, query, context));
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

    @Override
    public Mono<ResponseEntity<String>> executar(String authorization, String correlationId, UUID historicoEscolarId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalHistoricoEscolarWritePort.excluir(historicoEscolarId, query, context));
    }
}

