package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarDashboardAcademicoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DashboardAcademicoReadController {

    private final ConsultarDashboardAcademicoUseCase consultarDashboardAcademicoUseCase;

    public DashboardAcademicoReadController(ConsultarDashboardAcademicoUseCase consultarDashboardAcademicoUseCase) {
        this.consultarDashboardAcademicoUseCase = consultarDashboardAcademicoUseCase;
    }

    @GetMapping("/api/dashboard/academico")
    public Mono<ResponseEntity<String>> consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarDashboardAcademicoUseCase.consultar(authorization, correlationId);
    }
}
