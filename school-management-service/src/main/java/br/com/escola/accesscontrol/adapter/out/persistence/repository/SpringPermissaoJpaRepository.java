package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.PermissaoEntity;

@Repository
public interface SpringPermissaoJpaRepository extends JpaRepository<PermissaoEntity, UUID> {
	
    boolean existsByCodigo(String codigo);
    Optional<PermissaoEntity> findByCodigoIgnoreCase(String codigo);
}
