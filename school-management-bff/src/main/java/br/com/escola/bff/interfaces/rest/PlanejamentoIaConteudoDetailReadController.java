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
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoDetailUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.planning-ai-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PlanejamentoIaConteudoDetailReadController {

    private final ConsultarPlanejamentoIaConteudoDetailUseCase consultarPlanejamentoIaConteudoDetailUseCase;

    public PlanejamentoIaConteudoDetailReadController(
            ConsultarPlanejamentoIaConteudoDetailUseCase consultarPlanejamentoIaConteudoDetailUseCase) {
        this.consultarPlanejamentoIaConteudoDetailUseCase = consultarPlanejamentoIaConteudoDetailUseCase;
    }

    @GetMapping("/api/ia/conteudos/{conteudoId}")
    public Mono<ResponseEntity<String>> buscarConteudo(
            @PathVariable UUID conteudoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPlanejamentoIaConteudoDetailUseCase.buscarConteudo(
                authorization,
                correlationId,
                conteudoId);
    }
}
