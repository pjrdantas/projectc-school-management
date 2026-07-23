package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.DocumentoAlunoMatriculaWritePort;
import br.com.escola.bff.application.usecase.DocumentoAlunoWriteUseCase;
import reactor.core.publisher.Mono;

public class DocumentoAlunoWriteProxyService implements DocumentoAlunoWriteUseCase {

    private final AuthContextPort authContextPort;
    private final DocumentoAlunoMatriculaWritePort documentoAlunoWritePort;

    public DocumentoAlunoWriteProxyService(
            AuthContextPort authContextPort,
            DocumentoAlunoMatriculaWritePort documentoAlunoWritePort) {
        this.authContextPort = authContextPort;
        this.documentoAlunoWritePort = documentoAlunoWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> documentoAlunoWritePort.criar(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> enviar(
            String authorization,
            String correlationId,
            UUID alunoId,
            String tipoDocumento,
            String numeroDocumento,
            String observacao,
            FilePart arquivo) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> documentoAlunoWritePort.enviar(
                        alunoId,
                        tipoDocumento,
                        numeroDocumento,
                        observacao,
                        arquivo,
                        query,
                        context));
    }

    @Override
    public Mono<ResponseEntity<byte[]>> baixar(String authorization, String correlationId, UUID documentoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> documentoAlunoWritePort.baixar(documentoId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> excluir(String authorization, String correlationId, UUID documentoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> documentoAlunoWritePort.excluir(documentoId, query, context));
    }
}
