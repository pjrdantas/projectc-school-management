package br.com.escola.matricula.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaDocumentoExigidoEntity;

public interface MatriculaDocumentoExigidoJpaRepository extends JpaRepository<MatriculaDocumentoExigidoEntity, UUID> {

    List<MatriculaDocumentoExigidoEntity> findByTipoMatriculaId(UUID tipoMatriculaId);
}
