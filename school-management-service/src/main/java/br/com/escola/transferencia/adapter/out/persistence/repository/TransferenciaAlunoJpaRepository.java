package br.com.escola.transferencia.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.transferencia.adapter.out.persistence.entity.TransferenciaAlunoEntity;

public interface TransferenciaAlunoJpaRepository extends JpaRepository<TransferenciaAlunoEntity, UUID> {

    @EntityGraph(attributePaths = { "aluno", "escolaOrigem" })
    List<TransferenciaAlunoEntity> findByAluno_IdOrderByCreatedAtDesc(UUID alunoId);

    void deleteByAluno_Id(UUID alunoId);
}
