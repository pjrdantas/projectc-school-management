package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.DocumentoWritePort;
import br.com.escola.bff.infra.config.DocumentoMatriculaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class DocumentoWriteClient extends AbstractDownstreamClientSupport implements DocumentoWritePort {

    private static final String UNAVAILABLE_MESSAGE = "Enrollment document service indisponivel";
    private static final String INTERNAL_ERROR_MESSAGE = "Enrollment document service retornou erro interno";

    private final WebClient webClient;
    private final DocumentoMatriculaServiceClientProperties properties;

    public DocumentoWriteClient(
            @Qualifier("enrollmentDocumentServiceWebClient") WebClient enrollmentDocumentServiceWebClient,
            DocumentoMatriculaServiceClientProperties properties) {
        this.webClient = enrollmentDocumentServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String requestBody, CatalogReadQuery query, AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/documentos")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, INTERNAL_ERROR_MESSAGE))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, UNAVAILABLE_MESSAGE));
    }

    @Override
    public Mono<ResponseEntity<String>> enviar(
            String entidadeTipo,
            UUID entidadeId,
            String tipoDocumento,
            String observacao,
            FilePart arquivo,
            CatalogReadQuery query,
            AuthSessionContext context) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("entidadeTipo", entidadeTipo);
        body.part("entidadeId", entidadeId.toString());
        body.part("tipoDocumento", tipoDocumento);
        if (observacao != null) {
            body.part("observacao", observacao);
        }
        MediaType contentType = arquivo.headers().getContentType();
        body.asyncPart("arquivo", arquivo.content(), DataBuffer.class)
                .filename(arquivo.filename())
                .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType);

        return webClient.post()
                .uri("/internal/v1/documentos/upload")
                .headers(headers -> addHeaders(headers, query, context))
                .body(BodyInserters.fromMultipartData(body.build()))
                .exchangeToMono(response -> handle(response, INTERNAL_ERROR_MESSAGE))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, UNAVAILABLE_MESSAGE));
    }

    @Override
    public Mono<ResponseEntity<byte[]>> baixar(
            UUID documentoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/documentos/{documentoId}/conteudo", documentoId)
                .headers(headers -> addHeaders(headers, query, context))
                .exchangeToMono(response -> handleBinary(response, INTERNAL_ERROR_MESSAGE))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, UNAVAILABLE_MESSAGE));
    }

    @Override
    public Mono<ResponseEntity<String>> excluir(
            UUID documentoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.delete()
                .uri("/internal/v1/documentos/{documentoId}", documentoId)
                .headers(headers -> addHeaders(headers, query, context))
                .exchangeToMono(response -> handle(response, INTERNAL_ERROR_MESSAGE))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, UNAVAILABLE_MESSAGE));
    }

    private void addHeaders(HttpHeaders headers, CatalogReadQuery query, AuthSessionContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, query.authorization());
        headers.set("X-Internal-Token", properties.internalToken());
        headers.set("X-Correlation-Id", query.correlationId());
        headers.set("X-Usuario-Id", context.usuarioId().toString());
        headers.set("X-Escola-Id", context.escolaId().toString());
    }
}
