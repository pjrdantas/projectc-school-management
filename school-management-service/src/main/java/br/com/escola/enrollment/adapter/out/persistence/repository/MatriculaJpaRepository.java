package br.com.escola.enrollment.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.escola.enrollment.adapter.out.persistence.entity.MatriculaEntity;

public interface MatriculaJpaRepository extends JpaRepository<MatriculaEntity, UUID>, JpaSpecificationExecutor<MatriculaEntity> {

}
