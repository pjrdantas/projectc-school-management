package br.com.escola.seguranca.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.seguranca.adapter.out.persistence.entity.PerfilPermissaoEntity;

public interface PerfilPermissaoJpaRepository extends JpaRepository<PerfilPermissaoEntity, UUID> {

    List<PerfilPermissaoEntity> findByPerfilId(UUID perfilId);
}
