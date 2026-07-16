package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ListarDashboardPublicoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DashboardPublicoReadController {

    private final ListarDashboardPublicoUseCase listarDashboardPublicoUseCase;

    public DashboardPublicoReadController(ListarDashboardPublicoUseCase listarDashboardPublicoUseCase) {
        this.listarDashboardPublicoUseCase = listarDashboardPublicoUseCase;
    }

    @GetMapping("/api/dashboard/configuracoes/publicos")
    public Mono<ResponseEntity<String>> listarPublicos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return listarDashboardPublicoUseCase.listarPublicos(authorization, correlationId);
    }
}
