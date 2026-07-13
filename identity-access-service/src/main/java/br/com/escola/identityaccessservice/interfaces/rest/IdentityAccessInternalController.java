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
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;
import br.com.escola.identityaccessservice.application.dto.SelecionarEscolaAtivaRequest;
import br.com.escola.identityaccessservice.application.port.in.IdentityAccessUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class IdentityAccessInternalController {

    private final IdentityAccessUseCase identityAccessUseCase;

    public IdentityAccessInternalController(IdentityAccessUseCase identityAccessUseCase) {
        this.identityAccessUseCase = identityAccessUseCase;
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
