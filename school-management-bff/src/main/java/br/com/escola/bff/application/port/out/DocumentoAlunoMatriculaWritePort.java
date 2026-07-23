package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface DocumentoAlunoMatriculaWritePort {

    Mono<ResponseEntity<String>> criar(String requestBody, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> enviar(
            UUID alunoId,
            String tipoDocumento,
            String numeroDocumento,
            String observacao,
            FilePart arquivo,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<byte[]>> baixar(UUID documentoId, CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> excluir(UUID documentoId, CatalogReadQuery query, AuthSessionContext context);
}
