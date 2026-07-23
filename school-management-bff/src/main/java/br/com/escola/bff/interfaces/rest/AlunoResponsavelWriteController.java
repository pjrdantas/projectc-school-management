package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.AlunoResponsavelWriteUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.responsibles-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class AlunoResponsavelWriteController {

    private final AlunoResponsavelWriteUseCase alunoResponsavelWriteUseCase;

    public AlunoResponsavelWriteController(AlunoResponsavelWriteUseCase alunoResponsavelWriteUseCase) {
        this.alunoResponsavelWriteUseCase = alunoResponsavelWriteUseCase;
    }

    @PostMapping("/api/alunos/{alunoId}/responsaveis")
    public Mono<ResponseEntity<String>> vincular(
            @PathVariable UUID alunoId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return alunoResponsavelWriteUseCase.vincular(authorization, correlationId, alunoId, requestBody);
    }

    @PostMapping("/api/alunos/{alunoId}/responsaveis/{responsavelId}")
    public Mono<ResponseEntity<String>> vincularPorPath(
            @PathVariable UUID alunoId,
            @PathVariable UUID responsavelId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return alunoResponsavelWriteUseCase.vincular(
                authorization,
                correlationId,
                alunoId,
                "{\"idResponsavel\":\"" + responsavelId + "\"}");
    }

    @DeleteMapping("/api/alunos/{alunoId}/responsaveis/{responsavelId}")
    public Mono<ResponseEntity<String>> desvincular(
            @PathVariable UUID alunoId,
            @PathVariable UUID responsavelId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return alunoResponsavelWriteUseCase.desvincular(authorization, correlationId, alunoId, responsavelId);
    }
}
