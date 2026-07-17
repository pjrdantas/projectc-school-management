package br.com.escola.pedagogicalservice.infra.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.pedagogicalservice.infra.database.entity.FrequenciaAlunoJpaEntity;

public interface FrequenciaAlunoJpaRepository extends JpaRepository<FrequenciaAlunoJpaEntity, UUID> {

    List<FrequenciaAlunoJpaEntity> findAllBySchoolIdAndAulaIdOrderByCreatedAtAscIdAsc(UUID schoolId, UUID aulaId);
}
