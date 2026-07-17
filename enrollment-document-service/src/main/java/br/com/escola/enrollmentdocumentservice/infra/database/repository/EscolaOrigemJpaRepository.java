package br.com.escola.enrollmentdocumentservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.EscolaOrigemJpaEntity;

public interface EscolaOrigemJpaRepository extends JpaRepository<EscolaOrigemJpaEntity, UUID> {
    List<EscolaOrigemJpaEntity> findAllBySchoolIdOrderByNomeEscolaAscIdAsc(UUID schoolId);
    Optional<EscolaOrigemJpaEntity> findByIdAndSchoolId(UUID id, UUID schoolId);
}
