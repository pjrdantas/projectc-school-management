package br.com.projeto.piloto.domain.port.inbound;

import java.util.List;
import java.util.Optional;

import br.com.projeto.piloto.domain.model.AuthPerfilModel;

public interface AuthPerfilUseCase {

    
    AuthPerfilModel create(AuthPerfilModel domain);
    AuthPerfilModel update(Long id, AuthPerfilModel domain);
    Optional<AuthPerfilModel> findById(Long id);
    List<AuthPerfilModel> listAll();
    void delete(Long id);
    
    boolean existsByNmPerfil(String nmPermissao);
}
