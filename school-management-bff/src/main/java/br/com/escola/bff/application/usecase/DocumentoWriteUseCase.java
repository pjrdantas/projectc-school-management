package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Mono;

public interface DocumentoWriteUseCase {

    Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody);

    Mono<ResponseEntity<String>> enviar(
            String authorization,
            String correlationId,
            String entidadeTipo,
            UUID entidadeId,
            String tipoDocumento,
            String observacao,
            FilePart arquivo);

    Mono<ResponseEntity<byte[]>> baixar(String authorization, String correlationId, UUID documentoId);

    Mono<ResponseEntity<String>> excluir(String authorization, String correlationId, UUID documentoId);
}
