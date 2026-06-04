package br.com.escola.matricula.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaDocumentoExigidoEntity;

public interface MatriculaDocumentoExigidoJpaRepository extends JpaRepository<MatriculaDocumentoExigidoEntity, UUID> {

    List<MatriculaDocumentoExigidoEntity> findByTipoMatricula_IdOrderByOrdemAsc(UUID tipoMatriculaId);

    Optional<MatriculaDocumentoExigidoEntity> findByTipoMatricula_IdAndTipoDocumento_Id(
            UUID tipoMatriculaId,
            UUID tipoDocumentoId);

    boolean existsByTipoMatricula_IdAndTipoDocumento_Id(UUID tipoMatriculaId, UUID tipoDocumentoId);
}
