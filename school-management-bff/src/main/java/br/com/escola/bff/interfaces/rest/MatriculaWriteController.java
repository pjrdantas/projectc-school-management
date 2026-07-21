package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.MatriculaWriteUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.enrollment-document-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class MatriculaWriteController {

    private final MatriculaWriteUseCase matriculaWriteUseCase;

    public MatriculaWriteController(MatriculaWriteUseCase matriculaWriteUseCase) {
        this.matriculaWriteUseCase = matriculaWriteUseCase;
    }

    @PostMapping("/api/matriculas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criar(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return matriculaWriteUseCase.criar(authorization, correlationId, requestBody);
    }

    @PutMapping("/api/matriculas/{matriculaId}")
    public Mono<ResponseEntity<String>> atualizar(
            @PathVariable UUID matriculaId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return matriculaWriteUseCase.atualizar(authorization, correlationId, matriculaId, requestBody);
    }

    @PatchMapping("/api/matriculas/{matriculaId}/status")
    public Mono<ResponseEntity<String>> atualizarStatus(
            @PathVariable UUID matriculaId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return matriculaWriteUseCase.atualizarStatus(authorization, correlationId, matriculaId, requestBody);
    }

    @DeleteMapping("/api/matriculas/{matriculaId}")
    public Mono<ResponseEntity<String>> cancelar(
            @PathVariable UUID matriculaId,
            @RequestBody(required = false) String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return matriculaWriteUseCase.cancelar(authorization, correlationId, matriculaId, requestBody);
    }
}
