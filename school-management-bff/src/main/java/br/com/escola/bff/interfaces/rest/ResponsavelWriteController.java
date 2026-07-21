package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ResponsavelWriteUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.responsibles-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class ResponsavelWriteController {

    private final ResponsavelWriteUseCase responsavelWriteUseCase;

    public ResponsavelWriteController(ResponsavelWriteUseCase responsavelWriteUseCase) {
        this.responsavelWriteUseCase = responsavelWriteUseCase;
    }

    @PostMapping("/api/responsaveis")
    public Mono<ResponseEntity<String>> criar(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return responsavelWriteUseCase.criar(authorization, correlationId, requestBody);
    }

    @PutMapping("/api/responsaveis/{id}")
    public Mono<ResponseEntity<String>> atualizar(
            @PathVariable UUID id,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return responsavelWriteUseCase.atualizar(authorization, correlationId, id, requestBody);
    }

    @DeleteMapping("/api/responsaveis/{id}")
    public Mono<ResponseEntity<String>> excluir(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return responsavelWriteUseCase.excluir(authorization, correlationId, id);
    }
}
