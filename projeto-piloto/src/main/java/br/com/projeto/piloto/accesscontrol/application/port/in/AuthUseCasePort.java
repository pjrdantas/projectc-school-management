package br.com.projeto.piloto.accesscontrol.application.port.in;

import br.com.projeto.piloto.accesscontrol.domain.model.AuthUsuarioModel;

public interface AuthUseCasePort {

    AuthUsuarioModel authenticate(String login, String senha);

    AuthUsuarioModel findByLogin(String login);
}
