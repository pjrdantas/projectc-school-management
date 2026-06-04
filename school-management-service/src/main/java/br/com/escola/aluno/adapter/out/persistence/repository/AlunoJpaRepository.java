package br.com.escola.aluno.adapter.out.persistence.repository;

import java.util.UUID;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;

public interface AlunoJpaRepository extends JpaRepository<AlunoEntity, UUID> {

    @EntityGraph(attributePaths = {"pessoa", "statusAluno"})
    Optional<AlunoEntity> findByPessoaCpf(String cpf);

    long countByAtivoTrue();

    long countByAtivoFalse();

    boolean existsByPessoaCpfAndIdNot(String cpf, UUID id);

    default Optional<AlunoEntity> findByCpf(String cpf) {
        return findByPessoaCpf(cpf);
    }

    default boolean existsByCpfAndIdNot(String cpf, UUID id) {
        return existsByPessoaCpfAndIdNot(cpf, id);
    }
}
