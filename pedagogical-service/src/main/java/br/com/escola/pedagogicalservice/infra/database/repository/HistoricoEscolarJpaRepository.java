package br.com.escola.pedagogicalservice.infra.database.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.pedagogicalservice.infra.database.entity.HistoricoEscolarJpaEntity;

public interface HistoricoEscolarJpaRepository extends JpaRepository<HistoricoEscolarJpaEntity, UUID> {

    Optional<HistoricoEscolarJpaEntity> findByIdAndSchoolId(UUID id, UUID schoolId);

    Optional<HistoricoEscolarJpaEntity> findFirstBySchoolIdAndAlunoIdAndMatriculaIdAndModoOrderByUpdatedAtDesc(
            UUID schoolId,
            UUID alunoId,
            UUID matriculaId,
            String modo);

    List<HistoricoEscolarJpaEntity> findBySchoolIdOrderByUpdatedAtDesc(UUID schoolId);

    List<HistoricoEscolarJpaEntity> findBySchoolIdAndAlunoIdOrderByUpdatedAtDesc(UUID schoolId, UUID alunoId);
}
