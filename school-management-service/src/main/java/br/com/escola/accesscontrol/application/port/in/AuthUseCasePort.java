package br.com.escola.accesscontrol.application.port.in;

import br.com.escola.accesscontrol.domain.model.AuthUsuarioModel;

public interface AuthUseCasePort {

    AuthUsuarioModel authenticate(String username, String senha);

    AuthUsuarioModel findByLogin(String username);
}