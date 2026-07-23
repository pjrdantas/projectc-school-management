package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarAuthSessionUseCase;
import br.com.escola.bff.application.usecase.GerenciarAutenticacaoUseCase;
import br.com.escola.bff.application.usecase.SelecionarEscolaAtivaUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.identity-access-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class AuthSessionController {

    private final ConsultarAuthSessionUseCase consultarAuthSessionUseCase;
    private final SelecionarEscolaAtivaUseCase selecionarEscolaAtivaUseCase;
    private final GerenciarAutenticacaoUseCase gerenciarAutenticacaoUseCase;

    public AuthSessionController(
            ConsultarAuthSessionUseCase consultarAuthSessionUseCase,
            SelecionarEscolaAtivaUseCase selecionarEscolaAtivaUseCase,
            GerenciarAutenticacaoUseCase gerenciarAutenticacaoUseCase) {
        this.consultarAuthSessionUseCase = consultarAuthSessionUseCase;
        this.selecionarEscolaAtivaUseCase = selecionarEscolaAtivaUseCase;
        this.gerenciarAutenticacaoUseCase = gerenciarAutenticacaoUseCase;
    }

    @PostMapping(path = "/api/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> login(
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestBody String requestBody) {
        return gerenciarAutenticacaoUseCase.login(requestBody, correlationId);
    }

    @PostMapping(path = "/api/auth/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> refresh(
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestBody String requestBody) {
        return gerenciarAutenticacaoUseCase.refresh(requestBody, correlationId);
    }

    @PostMapping(path = "/api/auth/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> logout(
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestBody String requestBody) {
        return gerenciarAutenticacaoUseCase.logout(requestBody, correlationId);
    }

    @GetMapping("/api/auth/escolas")
    public Mono<ResponseEntity<String>> listarEscolas(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAuthSessionUseCase.listarEscolas(authorization, correlationId);
    }

    @PostMapping(path = "/api/auth/escola-ativa", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> selecionarEscolaAtiva(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestBody String requestBody) {
        return selecionarEscolaAtivaUseCase.selecionarEscolaAtiva(authorization, correlationId, requestBody);
    }
}
