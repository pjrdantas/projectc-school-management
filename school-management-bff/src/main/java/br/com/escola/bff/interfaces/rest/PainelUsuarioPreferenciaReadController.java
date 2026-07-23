package br.com.escola.bff.interfaces.rest;

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
import br.com.escola.bff.application.usecase.ListarPainelUsuarioPreferenciaUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PainelUsuarioPreferenciaReadController {

    private final ListarPainelUsuarioPreferenciaUseCase useCase;

    public PainelUsuarioPreferenciaReadController(ListarPainelUsuarioPreferenciaUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/api/dashboard/usuarios/{usuarioId}/configuracoes")
    public Mono<ResponseEntity<String>> listar(
            @PathVariable UUID usuarioId,
            @RequestParam(required = false) UUID dashboardId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return useCase.listar(authorization, correlationId, usuarioId, dashboardId);
    }
}
