package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.DocumentoWriteUseCase;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/documentos")
@ConditionalOnProperty(name = "features.enrollment-document-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DocumentoWriteController {

    private final DocumentoWriteUseCase documentoWriteUseCase;

    public DocumentoWriteController(DocumentoWriteUseCase documentoWriteUseCase) {
        this.documentoWriteUseCase = documentoWriteUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criar(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return documentoWriteUseCase.criar(authorization, correlationId, requestBody);
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> enviar(
            @RequestPart("entidadeTipo") String entidadeTipo,
            @RequestPart("entidadeId") String entidadeId,
            @RequestPart("tipoDocumento") String tipoDocumento,
            @RequestPart(value = "observacao", required = false) String observacao,
            @RequestPart("arquivo") FilePart arquivo,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return documentoWriteUseCase.enviar(
                authorization,
                correlationId,
                entidadeTipo,
                UUID.fromString(entidadeId),
                tipoDocumento,
                observacao,
                arquivo);
    }

    @GetMapping("/{documentoId}/conteudo")
    public Mono<ResponseEntity<byte[]>> baixar(
            @PathVariable UUID documentoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return documentoWriteUseCase.baixar(authorization, correlationId, documentoId);
    }

    @DeleteMapping("/{documentoId}")
    public Mono<ResponseEntity<String>> excluir(
            @PathVariable UUID documentoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return documentoWriteUseCase.excluir(authorization, correlationId, documentoId);
    }
}
