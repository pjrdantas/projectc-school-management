package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarResponsavelUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.responsibles-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class ResponsavelReadController {

    private final ConsultarResponsavelUseCase consultarResponsavelUseCase;

    public ResponsavelReadController(ConsultarResponsavelUseCase consultarResponsavelUseCase) {
        this.consultarResponsavelUseCase = consultarResponsavelUseCase;
    }

    @GetMapping("/api/responsaveis")
    public Mono<ResponseEntity<String>> listarResponsaveis(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf) {
        return consultarResponsavelUseCase.listarResponsaveis(authorization, correlationId, nome, cpf);
    }

    @GetMapping("/api/responsaveis/{id}")
    public Mono<ResponseEntity<String>> buscarResponsavelPorId(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarResponsavelUseCase.buscarResponsavelPorId(authorization, correlationId, id);
    }
}
