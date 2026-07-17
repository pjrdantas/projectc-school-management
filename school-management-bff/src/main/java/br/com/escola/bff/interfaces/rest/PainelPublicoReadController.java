package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ListarPainelPublicoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PainelPublicoReadController {

    private final ListarPainelPublicoUseCase listarPainelPublicoUseCase;

    public PainelPublicoReadController(ListarPainelPublicoUseCase listarPainelPublicoUseCase) {
        this.listarPainelPublicoUseCase = listarPainelPublicoUseCase;
    }

    @GetMapping("/api/dashboard/configuracoes/publicos")
    public Mono<ResponseEntity<String>> listarPublicos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return listarPainelPublicoUseCase.listarPublicos(authorization, correlationId);
    }
}

