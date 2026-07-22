package br.com.escola.enrollmentdocumentservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.DocumentoAdministrativoJpaEntity;

public interface DocumentoAdministrativoJpaRepository extends JpaRepository<DocumentoAdministrativoJpaEntity, UUID> {
    Optional<DocumentoAdministrativoJpaEntity> findByIdAndSchoolId(UUID id, UUID schoolId);
    Optional<DocumentoAdministrativoJpaEntity> findByIdAndSchoolIdAndDataExclusaoIsNull(UUID id, UUID schoolId);

    List<DocumentoAdministrativoJpaEntity> findAllBySchoolIdAndEntidadeTipoAndEntidadeIdAndDataExclusaoIsNullOrderByDataUploadDescIdAsc(
            UUID schoolId,
            String entidadeTipo,
            UUID entidadeId);
}
