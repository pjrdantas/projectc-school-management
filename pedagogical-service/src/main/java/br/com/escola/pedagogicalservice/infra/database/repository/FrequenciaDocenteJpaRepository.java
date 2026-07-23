package br.com.escola.pedagogicalservice.infra.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.pedagogicalservice.infra.database.entity.FrequenciaDocenteJpaEntity;

public interface FrequenciaDocenteJpaRepository extends JpaRepository<FrequenciaDocenteJpaEntity, UUID> {

    List<FrequenciaDocenteJpaEntity> findAllBySchoolIdAndAulaIdOrderByCreatedAtAscIdAsc(UUID schoolId, UUID aulaId);
}
