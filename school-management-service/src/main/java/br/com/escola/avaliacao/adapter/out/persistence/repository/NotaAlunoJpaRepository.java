package br.com.escola.avaliacao.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.avaliacao.adapter.out.persistence.entity.NotaAlunoEntity;

public interface NotaAlunoJpaRepository extends JpaRepository<NotaAlunoEntity, UUID> {

    List<NotaAlunoEntity> findByMatriculaId(UUID matriculaId);

    List<NotaAlunoEntity> findByAvaliacaoId(UUID avaliacaoId);

    Optional<NotaAlunoEntity> findByAvaliacaoIdAndMatriculaId(UUID avaliacaoId, UUID matriculaId);
}
