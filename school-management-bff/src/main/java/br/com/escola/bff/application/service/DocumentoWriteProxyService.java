package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.DocumentoWritePort;
import br.com.escola.bff.application.usecase.DocumentoWriteUseCase;
import reactor.core.publisher.Mono;

public class DocumentoWriteProxyService implements DocumentoWriteUseCase {

    private final AuthContextPort authContextPort;
    private final DocumentoWritePort documentoWritePort;

    public DocumentoWriteProxyService(AuthContextPort authContextPort, DocumentoWritePort documentoWritePort) {
        this.authContextPort = authContextPort;
        this.documentoWritePort = documentoWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> documentoWritePort.criar(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> enviar(
            String authorization,
            String correlationId,
            String entidadeTipo,
            UUID entidadeId,
            String tipoDocumento,
            String observacao,
            FilePart arquivo) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> documentoWritePort.enviar(
                        entidadeTipo,
                        entidadeId,
                        tipoDocumento,
                        observacao,
                        arquivo,
                        query,
                        context));
    }

    @Override
    public Mono<ResponseEntity<byte[]>> baixar(String authorization, String correlationId, UUID documentoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> documentoWritePort.baixar(documentoId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> excluir(String authorization, String correlationId, UUID documentoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> documentoWritePort.excluir(documentoId, query, context));
    }
}
