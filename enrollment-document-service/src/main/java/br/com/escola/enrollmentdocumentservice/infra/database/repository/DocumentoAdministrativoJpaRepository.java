package br.com.escola.enrollmentdocumentservice.infra.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.DocumentoAdministrativoJpaEntity;

public interface DocumentoAdministrativoJpaRepository extends JpaRepository<DocumentoAdministrativoJpaEntity, UUID> {
    List<DocumentoAdministrativoJpaEntity> findAllBySchoolIdAndEntidadeTipoAndEntidadeIdOrderByDataUploadDescIdAsc(
            UUID schoolId,
            String entidadeTipo,
            UUID entidadeId);
}
