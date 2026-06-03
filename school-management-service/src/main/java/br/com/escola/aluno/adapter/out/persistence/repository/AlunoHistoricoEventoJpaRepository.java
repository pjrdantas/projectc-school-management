package br.com.escola.aluno.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoHistoricoEventoEntity;

public interface AlunoHistoricoEventoJpaRepository extends JpaRepository<AlunoHistoricoEventoEntity, UUID> {

    List<AlunoHistoricoEventoEntity> findByAlunoId(UUID alunoId);
}
