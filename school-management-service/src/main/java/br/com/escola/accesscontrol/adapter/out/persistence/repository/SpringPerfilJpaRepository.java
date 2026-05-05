package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.PerfilEntity;

@Repository
public interface SpringPerfilJpaRepository extends JpaRepository<PerfilEntity, UUID> {

    boolean existsByCodigo(String codigo);

    Optional<PerfilEntity> findByCodigo(String codigo);
}