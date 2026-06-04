package br.com.escola.frequencia.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaProfessorEntity;

public interface FrequenciaProfessorJpaRepository extends JpaRepository<FrequenciaProfessorEntity, UUID> {

    List<FrequenciaProfessorEntity> findByProfessorId(UUID professorId);

    List<FrequenciaProfessorEntity> findByAulaId(UUID aulaId);

    Optional<FrequenciaProfessorEntity> findByAulaIdAndProfessorId(UUID aulaId, UUID professorId);
}
