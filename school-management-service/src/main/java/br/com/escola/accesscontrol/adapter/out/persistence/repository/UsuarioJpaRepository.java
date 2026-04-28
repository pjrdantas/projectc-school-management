package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<UsuarioEntity> findByUsernameIgnoreCaseAndAtivoTrue(String username);
    Optional<UsuarioEntity> findByEmailIgnoreCaseAndAtivoTrue(String email);
}
