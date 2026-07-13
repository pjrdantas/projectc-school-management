package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.CriarAvaliacaoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.pedagogical-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class AvaliacaoWriteController {

    private final CriarAvaliacaoUseCase criarAvaliacaoUseCase;

    public AvaliacaoWriteController(CriarAvaliacaoUseCase criarAvaliacaoUseCase) {
        this.criarAvaliacaoUseCase = criarAvaliacaoUseCase;
    }

    @PostMapping("/api/avaliacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criar(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return criarAvaliacaoUseCase.criar(authorization, correlationId, requestBody);
    }
}
