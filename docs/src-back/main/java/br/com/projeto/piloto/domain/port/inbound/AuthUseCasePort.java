package br.com.projeto.piloto.domain.port.inbound;

import br.com.projeto.piloto.domain.model.AuthUsuarioModel;

public interface AuthUseCasePort {

    AuthUsuarioModel authenticate(String login, String senha);

    AuthUsuarioModel findByLogin(String login);
}
