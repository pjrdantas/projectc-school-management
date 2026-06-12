package br.com.escola.aluno.adapter.out.persistence.repository;

import java.util.UUID;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;

public interface AlunoJpaRepository extends JpaRepository<AlunoEntity, UUID> {

    @EntityGraph(attributePaths = {"pessoa", "pessoa.escola", "statusAluno"})
    Optional<AlunoEntity> findByPessoaCpf(String cpf);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.escola", "statusAluno"})
    Optional<AlunoEntity> findByPessoa_CpfAndPessoa_Escola_Id(String cpf, UUID escolaId);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.escola", "statusAluno"})
    Optional<AlunoEntity> findByIdAndPessoa_Escola_Id(UUID id, UUID escolaId);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.escola", "statusAluno"})
    List<AlunoEntity> findAllByPessoa_Escola_Id(UUID escolaId);

    boolean existsByIdAndPessoa_Escola_Id(UUID id, UUID escolaId);

    long countByAtivoTrue();

    long countByAtivoFalse();

    long countByPessoa_Escola_IdAndAtivoTrue(UUID escolaId);

    long countByPessoa_Escola_IdAndAtivoFalse(UUID escolaId);

    boolean existsByPessoaCpfAndIdNot(String cpf, UUID id);

    boolean existsByPessoa_CpfAndPessoa_Escola_IdAndIdNot(String cpf, UUID escolaId, UUID id);

    default Optional<AlunoEntity> findByCpf(String cpf) {
        return findByPessoaCpf(cpf);
    }

    default Optional<AlunoEntity> findByCpfAndEscolaId(String cpf, UUID escolaId) {
        return findByPessoa_CpfAndPessoa_Escola_Id(cpf, escolaId);
    }

    default boolean existsByCpfAndIdNot(String cpf, UUID id) {
        return existsByPessoaCpfAndIdNot(cpf, id);
    }

    default boolean existsByCpfAndEscolaIdAndIdNot(String cpf, UUID escolaId, UUID id) {
        return existsByPessoa_CpfAndPessoa_Escola_IdAndIdNot(cpf, escolaId, id);
    }
}
