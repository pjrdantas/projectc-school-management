package br.com.escola.seguranca.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.seguranca.domain.model.UsuarioModel;

public interface UsuarioUseCasePort {

    UsuarioModel create(UsuarioModel usuario);

    UsuarioModel update(UUID id, UsuarioModel usuario);
    
    void delete(UUID id);

    UsuarioModel findById(UUID id);

    UsuarioModel findByUsername(String username);

    List<UsuarioModel> listAll();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}