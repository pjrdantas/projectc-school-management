package br.com.escola.historico.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.historico.adapter.out.persistence.entity.BoletimEntity;

public interface BoletimJpaRepository extends JpaRepository<BoletimEntity, UUID> {

    List<BoletimEntity> findByMatriculaId(UUID matriculaId);
}
