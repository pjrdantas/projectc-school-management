package br.com.projeto.piloto.accesscontrol.application.port.out;

import java.util.List;
import java.util.Optional;

import br.com.projeto.piloto.accesscontrol.domain.model.AuthPermissaoModel;

public interface AuthPermissaoRepositoryPort {

    List<AuthPermissaoModel> findAll();

    Optional<AuthPermissaoModel> findById(Long id);

    AuthPermissaoModel save(AuthPermissaoModel domain);

    void deleteById(Long id);

    boolean existsByNmPermissao(String nmPermissao);
}
