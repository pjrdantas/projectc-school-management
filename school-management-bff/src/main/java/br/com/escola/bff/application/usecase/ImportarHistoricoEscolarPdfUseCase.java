package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Mono;

public interface ImportarHistoricoEscolarPdfUseCase {

    Mono<ResponseEntity<String>> executar(String authorization, String correlationId, FilePart arquivo);
}
