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
import br.com.escola.bff.application.usecase.ListarPainelIndicadorSnapshotUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PainelIndicadorSnapshotReadController {

    private final ListarPainelIndicadorSnapshotUseCase listarPainelIndicadorSnapshotUseCase;

    public PainelIndicadorSnapshotReadController(
            ListarPainelIndicadorSnapshotUseCase listarPainelIndicadorSnapshotUseCase) {
        this.listarPainelIndicadorSnapshotUseCase = listarPainelIndicadorSnapshotUseCase;
    }

    @GetMapping("/api/dashboard/snapshots/publicos/{publicoCodigo}")
    public Mono<ResponseEntity<String>> listarPorPublicoCodigo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @PathVariable String publicoCodigo,
            @RequestParam(required = false) LocalDate referenciaData) {
        return listarPainelIndicadorSnapshotUseCase.listarPorPublicoCodigo(
                authorization,
                correlationId,
                publicoCodigo,
                referenciaData);
    }
}

