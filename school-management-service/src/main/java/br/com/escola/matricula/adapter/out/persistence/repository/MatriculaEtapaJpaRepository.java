package br.com.escola.matricula.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEtapaEntity;

public interface MatriculaEtapaJpaRepository extends JpaRepository<MatriculaEtapaEntity, UUID> {

    List<MatriculaEtapaEntity> findByMatricula_IdOrderByOrdem(UUID matriculaId);

    Optional<MatriculaEtapaEntity> findByIdAndMatricula_Id(UUID id, UUID matriculaId);

    void deleteByMatricula_Id(UUID matriculaId);
}
