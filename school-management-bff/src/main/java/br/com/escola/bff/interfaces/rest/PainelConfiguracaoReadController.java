package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ListarPainelConfiguracaoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PainelConfiguracaoReadController {

    private final ListarPainelConfiguracaoUseCase listarPainelConfiguracaoUseCase;

    public PainelConfiguracaoReadController(ListarPainelConfiguracaoUseCase listarPainelConfiguracaoUseCase) {
        this.listarPainelConfiguracaoUseCase = listarPainelConfiguracaoUseCase;
    }

    @GetMapping("/api/dashboard/configuracoes/dashboards")
    public Mono<ResponseEntity<String>> listarPainels(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestParam(required = false) UUID publicoPainelId,
            @RequestParam(required = false) String publicoCodigo) {
        return listarPainelConfiguracaoUseCase.listarPainels(
                authorization,
                correlationId,
                publicoPainelId,
                publicoCodigo);
    }
}

