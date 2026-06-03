package br.com.escola.matricula.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.matricula.adapter.out.persistence.entity.TipoMatriculaEntity;

public interface TipoMatriculaJpaRepository extends JpaRepository<TipoMatriculaEntity, UUID> {

    Optional<TipoMatriculaEntity> findByCodigoIgnoreCase(String codigo);
}

