package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarAvaliacaoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.pedagogical-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class AvaliacaoReadController {

    private final ConsultarAvaliacaoUseCase consultarAvaliacaoUseCase;

    public AvaliacaoReadController(ConsultarAvaliacaoUseCase consultarAvaliacaoUseCase) {
        this.consultarAvaliacaoUseCase = consultarAvaliacaoUseCase;
    }

    @GetMapping("/api/avaliacoes")
    public Mono<ResponseEntity<String>> listar(
            @RequestParam(required = false) UUID professorTurmaDisciplinaId,
            @RequestParam(required = false) UUID turmaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAvaliacaoUseCase.listar(authorization, correlationId, professorTurmaDisciplinaId, turmaId);
    }

    @GetMapping("/api/avaliacoes/{id}")
    public Mono<ResponseEntity<String>> buscarPorId(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAvaliacaoUseCase.buscarPorId(authorization, correlationId, id);
    }
}
