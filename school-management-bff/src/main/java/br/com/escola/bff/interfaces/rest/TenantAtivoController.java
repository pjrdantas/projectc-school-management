package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarTenantAtivoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.institutional-tenant-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class TenantAtivoController {

    private final ConsultarTenantAtivoUseCase consultarTenantAtivoUseCase;

    public TenantAtivoController(ConsultarTenantAtivoUseCase consultarTenantAtivoUseCase) {
        this.consultarTenantAtivoUseCase = consultarTenantAtivoUseCase;
    }

    @GetMapping("/api/auth/tenant/ativa")
    public Mono<ResponseEntity<String>> consultarTenantAtivo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarTenantAtivoUseCase.consultarTenantAtivo(authorization, correlationId);
    }
}

