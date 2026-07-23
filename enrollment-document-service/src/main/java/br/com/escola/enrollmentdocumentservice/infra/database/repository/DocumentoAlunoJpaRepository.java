package br.com.escola.enrollmentdocumentservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.DocumentoAlunoJpaEntity;

public interface DocumentoAlunoJpaRepository extends JpaRepository<DocumentoAlunoJpaEntity, UUID> {
    List<DocumentoAlunoJpaEntity> findAllBySchoolIdAndAlunoIdAndDataExclusaoIsNullOrderByDataUploadDescIdAsc(
            UUID schoolId,
            UUID alunoId);
    Optional<DocumentoAlunoJpaEntity> findByIdAndSchoolId(UUID id, UUID schoolId);
    Optional<DocumentoAlunoJpaEntity> findByIdAndSchoolIdAndDataExclusaoIsNull(UUID id, UUID schoolId);
}
