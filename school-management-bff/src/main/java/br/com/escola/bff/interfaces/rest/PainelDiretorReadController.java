package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPainelDiretorUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PainelDiretorReadController {

    private final ConsultarPainelDiretorUseCase consultarPainelDiretorUseCase;

    public PainelDiretorReadController(ConsultarPainelDiretorUseCase consultarPainelDiretorUseCase) {
        this.consultarPainelDiretorUseCase = consultarPainelDiretorUseCase;
    }

    @GetMapping("/api/dashboard/diretor")
    public Mono<ResponseEntity<String>> consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPainelDiretorUseCase.consultar(authorization, correlationId);
    }
}

