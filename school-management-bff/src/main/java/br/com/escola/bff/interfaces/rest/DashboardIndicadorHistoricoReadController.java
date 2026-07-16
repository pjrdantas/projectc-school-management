package br.com.escola.bff.interfaces.rest;

import java.time.LocalDate;
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
import br.com.escola.bff.application.usecase.ConsultarDashboardIndicadorHistoricoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DashboardIndicadorHistoricoReadController {

    private final ConsultarDashboardIndicadorHistoricoUseCase consultarDashboardIndicadorHistoricoUseCase;

    public DashboardIndicadorHistoricoReadController(
            ConsultarDashboardIndicadorHistoricoUseCase consultarDashboardIndicadorHistoricoUseCase) {
        this.consultarDashboardIndicadorHistoricoUseCase = consultarDashboardIndicadorHistoricoUseCase;
    }

    @GetMapping("/api/dashboard/snapshots/historico/publicos/{publicoCodigo}")
    public Mono<ResponseEntity<String>> consultarHistorico(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @PathVariable String publicoCodigo,
            @RequestParam(required = false) String codigoIndicador,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) UUID professorId) {
        return consultarDashboardIndicadorHistoricoUseCase.consultarHistorico(
                authorization,
                correlationId,
                publicoCodigo,
                codigoIndicador,
                dataInicio,
                dataFim,
                professorId);
    }
}
