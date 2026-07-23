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
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoBimestralUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.planning-ai-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PlanejamentoBimestralReadController {

    private final ConsultarPlanejamentoBimestralUseCase consultarPlanejamentoBimestralUseCase;

    public PlanejamentoBimestralReadController(
            ConsultarPlanejamentoBimestralUseCase consultarPlanejamentoBimestralUseCase) {
        this.consultarPlanejamentoBimestralUseCase = consultarPlanejamentoBimestralUseCase;
    }

    @GetMapping("/api/planejamentos-bimestrais")
    public Mono<ResponseEntity<String>> listar(
            @RequestParam(required = false) UUID professorTurmaDisciplinaId,
            @RequestParam(required = false) UUID periodoAvaliativoId,
            @RequestParam(required = false) UUID professorId,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(required = false) UUID disciplinaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPlanejamentoBimestralUseCase.listar(
                authorization,
                correlationId,
                professorTurmaDisciplinaId,
                periodoAvaliativoId,
                professorId,
                turmaId,
                disciplinaId);
    }

    @GetMapping("/api/planejamentos-bimestrais/{planejamentoId}")
    public Mono<ResponseEntity<String>> buscarPorId(
            @PathVariable UUID planejamentoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPlanejamentoBimestralUseCase.buscarPorId(authorization, correlationId, planejamentoId);
    }
}
