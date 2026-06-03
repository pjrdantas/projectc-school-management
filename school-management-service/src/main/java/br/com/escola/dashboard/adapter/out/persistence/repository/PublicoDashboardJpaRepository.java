package br.com.escola.dashboard.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboard.adapter.out.persistence.entity.PublicoDashboardEntity;

public interface PublicoDashboardJpaRepository extends JpaRepository<PublicoDashboardEntity, UUID> {

    Optional<PublicoDashboardEntity> findByCodigo(String codigo);
}
