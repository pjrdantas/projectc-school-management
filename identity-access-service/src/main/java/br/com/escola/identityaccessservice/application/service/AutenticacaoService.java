package br.com.escola.identityaccessservice.application.service;

import org.springframework.stereotype.Service;

import br.com.escola.identityaccessservice.application.dto.AuthSessionResponse;
import br.com.escola.identityaccessservice.application.model.SessaoAutenticada;
import br.com.escola.identityaccessservice.application.port.in.AutenticacaoUseCase;
import br.com.escola.identityaccessservice.application.port.out.CredencialSessaoPort;

@Service
public class AutenticacaoService implements AutenticacaoUseCase {

    private final CredencialSessaoPort credencialSessaoPort;

    public AutenticacaoService(CredencialSessaoPort credencialSessaoPort) {
        this.credencialSessaoPort = credencialSessaoPort;
    }

    @Override
    public AuthSessionResponse autenticar(String login, String senha) {
        return toResponse(credencialSessaoPort.autenticar(login.trim(), senha));
    }

    @Override
    public AuthSessionResponse renovar(String refreshToken) {
        return toResponse(credencialSessaoPort.renovar(refreshToken.trim()));
    }

    @Override
    public void encerrar(String refreshToken) {
        credencialSessaoPort.encerrar(refreshToken.trim());
    }

    private AuthSessionResponse toResponse(SessaoAutenticada sessao) {
        return new AuthSessionResponse(
                sessao.accessToken(),
                sessao.refreshToken(),
                "Bearer",
                sessao.usuarioId(),
                null,
                sessao.username(),
                sessao.username(),
                sessao.nome(),
                sessao.perfis(),
                sessao.permissoes());
    }
}
