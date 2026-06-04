package br.com.escola.aluno.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.aluno.adapter.out.persistence.entity.SolicitacaoExclusaoAlunoEntity;

public interface SolicitacaoExclusaoAlunoJpaRepository extends JpaRepository<SolicitacaoExclusaoAlunoEntity, UUID> {

    List<SolicitacaoExclusaoAlunoEntity> findByAlunoId(UUID alunoId);

    long countByStatusIgnoreCase(String status);
}
