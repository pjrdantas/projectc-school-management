package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarFuncionarioUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.people-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class FuncionarioReadController {

    private final ConsultarFuncionarioUseCase consultarFuncionarioUseCase;

    public FuncionarioReadController(ConsultarFuncionarioUseCase consultarFuncionarioUseCase) {
        this.consultarFuncionarioUseCase = consultarFuncionarioUseCase;
    }

    @GetMapping("/api/funcionarios")
    public Mono<ResponseEntity<String>> listarFuncionarios(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarFuncionarioUseCase.listarFuncionarios(authorization, correlationId);
    }

    @GetMapping("/api/funcionarios/{funcionarioId}")
    public Mono<ResponseEntity<String>> buscarFuncionarioPorId(
            @PathVariable UUID funcionarioId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarFuncionarioUseCase.buscarFuncionarioPorId(authorization, correlationId, funcionarioId);
    }
}
