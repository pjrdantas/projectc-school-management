package br.com.escola.identityaccessservice.application.port.in;

import br.com.escola.identityaccessservice.application.dto.AuthSessionResponse;

public interface AutenticacaoUseCase {

    AuthSessionResponse autenticar(String login, String senha);

    AuthSessionResponse renovar(String refreshToken);

    void encerrar(String refreshToken);
}
