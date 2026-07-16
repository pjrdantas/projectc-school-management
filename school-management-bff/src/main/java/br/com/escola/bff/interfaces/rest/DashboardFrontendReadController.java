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
import br.com.escola.bff.application.usecase.ConsultarDashboardFrontendUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DashboardFrontendReadController {

    private final ConsultarDashboardFrontendUseCase consultarDashboardFrontendUseCase;

    public DashboardFrontendReadController(ConsultarDashboardFrontendUseCase consultarDashboardFrontendUseCase) {
        this.consultarDashboardFrontendUseCase = consultarDashboardFrontendUseCase;
    }

    @GetMapping("/api/dashboard/frontend")
    public Mono<ResponseEntity<String>> consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestParam String publicoCodigo,
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) UUID professorId) {
        return consultarDashboardFrontendUseCase.consultar(
                authorization,
                correlationId,
                publicoCodigo,
                usuarioId,
                professorId);
    }
}
