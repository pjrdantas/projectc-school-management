package br.com.escola.frequencia.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.frequencia.adapter.out.persistence.entity.SituacaoFrequenciaEntity;

public interface SituacaoFrequenciaJpaRepository extends JpaRepository<SituacaoFrequenciaEntity, UUID> {

    Optional<SituacaoFrequenciaEntity> findByCodigo(String codigo);
}
