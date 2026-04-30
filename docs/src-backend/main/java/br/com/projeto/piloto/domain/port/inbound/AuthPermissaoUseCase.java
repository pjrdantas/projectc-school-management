package br.com.projeto.piloto.domain.port.inbound;

import java.util.List;
import java.util.Optional;

import br.com.projeto.piloto.domain.model.AuthPermissaoModel;

public interface AuthPermissaoUseCase {

    List<AuthPermissaoModel> listAll();

    Optional<AuthPermissaoModel> findById(Long id);

    AuthPermissaoModel create(AuthPermissaoModel domain);

    AuthPermissaoModel update(Long id, AuthPermissaoModel domain);

    void delete(Long id);

    boolean existsByNmPermissao(String nmPermissao);
}
