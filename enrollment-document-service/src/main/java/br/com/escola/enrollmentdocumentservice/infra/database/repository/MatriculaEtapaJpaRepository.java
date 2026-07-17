package br.com.escola.enrollmentdocumentservice.infra.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.MatriculaEtapaJpaEntity;

public interface MatriculaEtapaJpaRepository extends JpaRepository<MatriculaEtapaJpaEntity, UUID> {
    List<MatriculaEtapaJpaEntity> findAllByMatriculaIdOrderByOrdemAscIdAsc(UUID matriculaId);
}
