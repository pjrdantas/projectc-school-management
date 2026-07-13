package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.planning-ai-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PlanejamentoIaConteudoReadController {

    private final ConsultarPlanejamentoIaConteudoUseCase consultarPlanejamentoIaConteudoUseCase;

    public PlanejamentoIaConteudoReadController(
            ConsultarPlanejamentoIaConteudoUseCase consultarPlanejamentoIaConteudoUseCase) {
        this.consultarPlanejamentoIaConteudoUseCase = consultarPlanejamentoIaConteudoUseCase;
    }

    @GetMapping("/api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos")
    public Mono<ResponseEntity<String>> listarConteudos(
            @PathVariable UUID planejamentoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPlanejamentoIaConteudoUseCase.listarConteudos(
                authorization,
                correlationId,
                planejamentoId);
    }
}
