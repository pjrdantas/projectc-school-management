package br.com.escola.pedagogicalservice.infra.database.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.pedagogicalservice.infra.database.entity.DiarioClasseJpaEntity;

public interface DiarioClasseJpaRepository extends JpaRepository<DiarioClasseJpaEntity, String> {

    Optional<DiarioClasseJpaEntity> findByIdAndSchoolId(String id, UUID schoolId);

    Optional<DiarioClasseJpaEntity> findFirstBySchoolIdAndProfessorIdAndTurmaIdAndDisciplinaIdAndAnoLetivoAndMesAndDataReferenciaOrderByUpdatedAtDesc(
            UUID schoolId,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia);
}
