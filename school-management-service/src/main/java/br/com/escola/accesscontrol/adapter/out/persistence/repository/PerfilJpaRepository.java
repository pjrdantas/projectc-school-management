package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.PerfilEntity;

public interface PerfilJpaRepository extends JpaRepository<PerfilEntity, UUID> {
    boolean existsByCodigo(String codigo);
    Optional<PerfilEntity> findByCodigoIgnoreCase(String codigo);
}
