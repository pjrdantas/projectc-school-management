package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.PlanejamentoBimestralWriteUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.planning-ai-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PlanejamentoBimestralWriteController {

    private final PlanejamentoBimestralWriteUseCase planejamentoBimestralWriteUseCase;

    public PlanejamentoBimestralWriteController(PlanejamentoBimestralWriteUseCase planejamentoBimestralWriteUseCase) {
        this.planejamentoBimestralWriteUseCase = planejamentoBimestralWriteUseCase;
    }

    @PostMapping("/api/planejamentos-bimestrais")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criar(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return planejamentoBimestralWriteUseCase.criar(authorization, correlationId, requestBody);
    }

    @PutMapping("/api/planejamentos-bimestrais/{planejamentoId}")
    public Mono<ResponseEntity<String>> atualizar(
            @PathVariable UUID planejamentoId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return planejamentoBimestralWriteUseCase.atualizar(
                authorization, correlationId, planejamentoId, requestBody);
    }

    @PatchMapping("/api/planejamentos-bimestrais/{planejamentoId}/status")
    public Mono<ResponseEntity<String>> alterarStatus(
            @PathVariable UUID planejamentoId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return planejamentoBimestralWriteUseCase.alterarStatus(
                authorization, correlationId, planejamentoId, requestBody);
    }

    @PostMapping("/api/planejamentos-bimestrais/{planejamentoId}/aulas-previstas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> adicionarAula(
            @PathVariable UUID planejamentoId, @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return planejamentoBimestralWriteUseCase.adicionarAula(authorization, correlationId, planejamentoId, requestBody);
    }

    @PostMapping("/api/planejamentos-bimestrais/{planejamentoId}/avaliacoes-previstas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> adicionarAvaliacao(
            @PathVariable UUID planejamentoId, @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return planejamentoBimestralWriteUseCase.adicionarAvaliacao(authorization, correlationId, planejamentoId, requestBody);
    }
}
