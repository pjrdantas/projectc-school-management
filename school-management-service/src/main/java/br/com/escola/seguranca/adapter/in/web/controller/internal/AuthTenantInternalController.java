package br.com.escola.seguranca.adapter.in.web.controller.internal;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.seguranca.adapter.in.web.dto.AuthContextResponse;
import br.com.escola.seguranca.adapter.in.web.dto.internal.EscolaSessaoInternalResponse;
import br.com.escola.seguranca.adapter.in.web.dto.internal.SelecionarEscolaAtivaRequest;
import br.com.escola.seguranca.application.dto.internal.ContextoAutenticadoResumo;
import br.com.escola.seguranca.application.dto.internal.EscolaSessaoResumo;
import br.com.escola.seguranca.application.port.internal.IdentidadeTenantPort;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/auth")
public class AuthTenantInternalController {

    private final IdentidadeTenantPort identidadeTenantPort;

    public AuthTenantInternalController(IdentidadeTenantPort identidadeTenantPort) {
        this.identidadeTenantPort = identidadeTenantPort;
    }

    @GetMapping("/escolas")
    public List<EscolaSessaoInternalResponse> listarEscolas(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        return identidadeTenantPort.listarEscolasDisponiveis(extrairBearerToken(authorization)).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/escola-ativa")
    public ResponseEntity<AuthContextResponse> selecionarEscolaAtiva(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody SelecionarEscolaAtivaRequest request) {
        ContextoAutenticadoResumo contexto = identidadeTenantPort.selecionarEscolaAtiva(
                extrairBearerToken(authorization),
                request.escolaId());
        return ResponseEntity.ok(new AuthContextResponse(
                contexto.usuarioId(),
                contexto.escolaId(),
                contexto.escolaNome(),
                contexto.username()));
    }

    private EscolaSessaoInternalResponse toResponse(EscolaSessaoResumo resumo) {
        return new EscolaSessaoInternalResponse(
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.ativa());
    }

    private String extrairBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Bearer token obrigatorio");
        }
        String token = authorization.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Bearer token obrigatorio");
        }
        return token;
    }
}
