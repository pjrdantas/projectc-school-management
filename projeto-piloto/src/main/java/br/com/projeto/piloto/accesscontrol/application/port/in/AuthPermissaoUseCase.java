package br.com.projeto.piloto.accesscontrol.application.port.in;

import java.util.List;
import java.util.Optional;

import br.com.projeto.piloto.accesscontrol.domain.model.AuthPermissaoModel;

public interface AuthPermissaoUseCase {

    List<AuthPermissaoModel> listAll();

    Optional<AuthPermissaoModel> findById(Long id);

    AuthPermissaoModel create(AuthPermissaoModel domain);

    AuthPermissaoModel update(Long id, AuthPermissaoModel domain);

    void delete(Long id);

    boolean existsByNmPermissao(String nmPermissao);
}
