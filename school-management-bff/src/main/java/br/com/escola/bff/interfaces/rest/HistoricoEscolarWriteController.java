package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.AtualizarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.CriarHistoricoEscolarUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.pedagogical-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class HistoricoEscolarWriteController {

    private final CriarHistoricoEscolarUseCase criarHistoricoEscolarUseCase;
    private final AtualizarHistoricoEscolarUseCase atualizarHistoricoEscolarUseCase;

    public HistoricoEscolarWriteController(
            CriarHistoricoEscolarUseCase criarHistoricoEscolarUseCase,
            AtualizarHistoricoEscolarUseCase atualizarHistoricoEscolarUseCase) {
        this.criarHistoricoEscolarUseCase = criarHistoricoEscolarUseCase;
        this.atualizarHistoricoEscolarUseCase = atualizarHistoricoEscolarUseCase;
    }

    @PostMapping("/api/historicos-escolares")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criar(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return criarHistoricoEscolarUseCase.executar(authorization, correlationId, requestBody);
    }

    @PutMapping("/api/historicos-escolares/{id}")
    public Mono<ResponseEntity<String>> atualizar(
            @PathVariable UUID id,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return atualizarHistoricoEscolarUseCase.executar(authorization, correlationId, id, requestBody);
    }
}
