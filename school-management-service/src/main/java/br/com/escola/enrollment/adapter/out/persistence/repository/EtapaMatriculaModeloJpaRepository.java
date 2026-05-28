package br.com.escola.enrollment.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollment.adapter.out.persistence.entity.EtapaMatriculaModeloEntity;
import br.com.escola.enrollment.adapter.out.persistence.entity.TipoMatriculaEntity;

public interface EtapaMatriculaModeloJpaRepository extends JpaRepository<EtapaMatriculaModeloEntity, UUID> {

    List<EtapaMatriculaModeloEntity> findByTipoMatriculaOrderByOrdem(TipoMatriculaEntity tipoMatricula);
}

