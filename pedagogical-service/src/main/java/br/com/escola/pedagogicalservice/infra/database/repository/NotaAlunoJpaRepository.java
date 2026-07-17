package br.com.escola.pedagogicalservice.infra.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.pedagogicalservice.infra.database.entity.NotaAlunoJpaEntity;

public interface NotaAlunoJpaRepository extends JpaRepository<NotaAlunoJpaEntity, UUID> {

    List<NotaAlunoJpaEntity> findAllBySchoolIdAndAvaliacaoIdOrderByCreatedAtAscIdAsc(UUID schoolId, UUID avaliacaoId);

    List<NotaAlunoJpaEntity> findAllBySchoolIdAndMatriculaIdOrderByCreatedAtAscIdAsc(UUID schoolId, UUID matriculaId);
}
