package br.com.escola.pedagogicalservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.pedagogicalservice.infra.database.entity.BoletimJpaEntity;

public interface BoletimJpaRepository extends JpaRepository<BoletimJpaEntity, UUID> {

    Optional<BoletimJpaEntity> findFirstBySchoolIdAndMatriculaIdAndFechamentoFalseOrderByUpdatedAtDesc(UUID schoolId, UUID matriculaId);

    List<BoletimJpaEntity> findAllBySchoolIdAndMatriculaIdAndFechamentoTrueOrderByUpdatedAtDescIdAsc(UUID schoolId, UUID matriculaId);
}
