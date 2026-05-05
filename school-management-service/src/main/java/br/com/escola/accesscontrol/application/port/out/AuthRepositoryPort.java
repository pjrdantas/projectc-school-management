package br.com.escola.accesscontrol.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;

public interface AuthRepositoryPort {

    UsuarioEntity save(UsuarioEntity usuario);

    Optional<UsuarioEntity> findById(UUID id);

    Optional<UsuarioEntity> findByUsername(String username);

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<UsuarioEntity> findAll();

    void deleteById(UUID id);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    boolean existsById(UUID id);
}