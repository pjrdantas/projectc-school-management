package br.com.escola.planejamento.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planejamento.adapter.out.persistence.entity.StatusPlanejamentoEntity;

public interface StatusPlanejamentoJpaRepository extends JpaRepository<StatusPlanejamentoEntity, UUID> {

    Optional<StatusPlanejamentoEntity> findByCodigo(String codigo);
}
