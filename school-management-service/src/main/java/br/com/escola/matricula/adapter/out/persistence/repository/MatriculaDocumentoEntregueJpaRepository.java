package br.com.escola.matricula.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaDocumentoEntregueEntity;

public interface MatriculaDocumentoEntregueJpaRepository extends JpaRepository<MatriculaDocumentoEntregueEntity, UUID> {

    List<MatriculaDocumentoEntregueEntity> findByMatriculaId(UUID matriculaId);
}
