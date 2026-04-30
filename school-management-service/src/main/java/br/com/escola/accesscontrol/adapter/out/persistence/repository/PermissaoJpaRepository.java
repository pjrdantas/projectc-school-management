package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.PermissaoEntity;

public interface PermissaoJpaRepository extends JpaRepository<PermissaoEntity, UUID> {
    boolean existsByCodigo(String codigo);
    Optional<PermissaoEntity> findByCodigoIgnoreCase(String codigo);
}
