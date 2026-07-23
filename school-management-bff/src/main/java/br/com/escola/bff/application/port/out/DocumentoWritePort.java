package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface DocumentoWritePort {

    Mono<ResponseEntity<String>> criar(String requestBody, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> enviar(
            String entidadeTipo,
            UUID entidadeId,
            String tipoDocumento,
            String observacao,
            FilePart arquivo,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<byte[]>> baixar(UUID documentoId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> excluir(UUID documentoId, CatalogReadQuery query, AuthSessionContext context);
}
