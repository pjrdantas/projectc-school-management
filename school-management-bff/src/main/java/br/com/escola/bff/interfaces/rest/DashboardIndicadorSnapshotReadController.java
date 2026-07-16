package br.com.escola.bff.interfaces.rest;

import java.time.LocalDate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ListarDashboardIndicadorSnapshotUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DashboardIndicadorSnapshotReadController {

    private final ListarDashboardIndicadorSnapshotUseCase listarDashboardIndicadorSnapshotUseCase;

    public DashboardIndicadorSnapshotReadController(
            ListarDashboardIndicadorSnapshotUseCase listarDashboardIndicadorSnapshotUseCase) {
        this.listarDashboardIndicadorSnapshotUseCase = listarDashboardIndicadorSnapshotUseCase;
    }

    @GetMapping("/api/dashboard/snapshots/publicos/{publicoCodigo}")
    public Mono<ResponseEntity<String>> listarPorPublicoCodigo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @PathVariable String publicoCodigo,
            @RequestParam(required = false) LocalDate referenciaData) {
        return listarDashboardIndicadorSnapshotUseCase.listarPorPublicoCodigo(
                authorization,
                correlationId,
                publicoCodigo,
                referenciaData);
    }
}
