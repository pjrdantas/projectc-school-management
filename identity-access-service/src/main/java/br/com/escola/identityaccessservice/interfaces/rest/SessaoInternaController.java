package br.com.escola.identityaccessservice.interfaces.rest;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.identityaccessservice.application.context.InternalHeaders;
import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.dto.AuthSessionResponse;
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;
import br.com.escola.identityaccessservice.application.dto.LoginRequest;
import br.com.escola.identityaccessservice.application.dto.RefreshTokenRequest;
import br.com.escola.identityaccessservice.application.dto.SelecionarEscolaAtivaRequest;
import br.com.escola.identityaccessservice.application.port.in.AutenticacaoUseCase;
import br.com.escola.identityaccessservice.application.port.in.SessaoAutenticadaUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class SessaoInternaController {

    private final SessaoAutenticadaUseCase identityAccessUseCase;
    private final AutenticacaoUseCase autenticacaoUseCase;

    public SessaoInternaController(
            SessaoAutenticadaUseCase identityAccessUseCase,
            AutenticacaoUseCase autenticacaoUseCase) {
        this.identityAccessUseCase = identityAccessUseCase;
        this.autenticacaoUseCase = autenticacaoUseCase;
    }

    @PostMapping("/auth/login")
    public AuthSessionResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticacaoUseCase.autenticar(request.login(), request.senha());
    }

    @PostMapping("/auth/refresh")
    public AuthSessionResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return autenticacaoUseCase.renovar(request.refreshToken());
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        autenticacaoUseCase.encerrar(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/contexto-atual")
    public ResponseEntity<AuthContextResponse> consultarContextoAtual(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return ResponseEntity.ok(identityAccessUseCase.consultarContextoAtual(authorization, context));
    }

    @GetMapping("/auth/escolas")
    public List<EscolaSessaoResponse> listarEscolasDisponiveis(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return identityAccessUseCase.listarEscolasDisponiveis(authorization, context);
    }

    @PostMapping("/auth/escola-ativa")
    public ResponseEntity<AuthContextResponse> selecionarEscolaAtiva(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @Valid @RequestBody SelecionarEscolaAtivaRequest request) {
        return ResponseEntity.ok(identityAccessUseCase.selecionarEscolaAtiva(
                authorization,
                context,
                request.escolaId()));
    }
}

