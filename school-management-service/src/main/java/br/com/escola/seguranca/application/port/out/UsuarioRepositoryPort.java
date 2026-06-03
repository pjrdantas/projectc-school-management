package br.com.escola.seguranca.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;

public interface UsuarioRepositoryPort {

    UsuarioEntity save(UsuarioEntity usuario);

    Optional<UsuarioEntity> findById(UUID id);

    Optional<UsuarioEntity> findByUsername(String username);

    List<UsuarioEntity> findAll();

    void deleteById(UUID id);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsById(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}