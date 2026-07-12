package br.com.escola.seguranca.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.seguranca.adapter.in.web.dto.AuthContextResponse;
import br.com.escola.seguranca.adapter.in.web.dto.AuthResponse;
import br.com.escola.seguranca.application.dto.internal.ContextoAutenticadoResumo;
import br.com.escola.seguranca.application.dto.internal.SessaoAutenticadaResumo;
import br.com.escola.seguranca.application.port.internal.IdentidadeTenantPort;

@Service
public class AuthService {
    private final IdentidadeTenantPort identidadeTenantPort;

    public AuthService(IdentidadeTenantPort identidadeTenantPort) {
        this.identidadeTenantPort = identidadeTenantPort;
    }

    public AuthResponse login(String login, String senha, UUID escolaId) {
        return toAuthResponse(identidadeTenantPort.autenticar(login, senha, escolaId));
    }

    public AuthResponse refresh(String refreshToken) {
        return toAuthResponse(identidadeTenantPort.renovarSessao(refreshToken));
    }

    public void logout(String refreshToken) {
        identidadeTenantPort.encerrarSessao(refreshToken);
    }

    public AuthContextResponse contextoAtual(String accessToken) {
        return toAuthContextResponse(identidadeTenantPort.resolverContextoAtual(accessToken));
    }

    private AuthResponse toAuthResponse(SessaoAutenticadaResumo resumo) {
        return new AuthResponse(
                resumo.accessToken(),
                resumo.refreshToken(),
                resumo.tokenType(),
                resumo.usuarioId(),
                resumo.professorId(),
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.username(),
                resumo.nome(),
                resumo.perfis(),
                resumo.permissoes());
    }

    private AuthContextResponse toAuthContextResponse(ContextoAutenticadoResumo contexto) {
        return new AuthContextResponse(
                contexto.usuarioId(),
                contexto.escolaId(),
                contexto.escolaNome(),
                contexto.username());
    }
}
