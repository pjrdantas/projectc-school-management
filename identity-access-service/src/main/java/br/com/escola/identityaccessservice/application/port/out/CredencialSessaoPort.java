package br.com.escola.identityaccessservice.application.port.out;

import br.com.escola.identityaccessservice.application.model.SessaoAutenticada;

public interface CredencialSessaoPort {

    SessaoAutenticada autenticar(String login, String senha);

    SessaoAutenticada renovar(String refreshToken);

    void encerrar(String refreshToken);
}
