package br.com.escola.responsavel.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.responsavel.adapter.out.persistence.entity.ResponsavelEntity;

public interface ResponsavelJpaRepository extends JpaRepository<ResponsavelEntity, UUID> {

    @EntityGraph(attributePaths = {"pessoa", "pessoa.escola"})
    Optional<ResponsavelEntity> findByPessoaCpf(String cpf);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.escola"})
    Optional<ResponsavelEntity> findByPessoa_CpfAndPessoa_Escola_Id(String cpf, UUID escolaId);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.escola"})
    Optional<ResponsavelEntity> findByIdAndPessoa_Escola_Id(UUID id, UUID escolaId);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.escola"})
    List<ResponsavelEntity> findAllByPessoa_Escola_Id(UUID escolaId);

    boolean existsByIdAndPessoa_Escola_Id(UUID id, UUID escolaId);

    boolean existsByPessoaCpfAndIdNot(String cpf, UUID id);

    boolean existsByPessoa_CpfAndPessoa_Escola_IdAndIdNot(String cpf, UUID escolaId, UUID id);

    default Optional<ResponsavelEntity> findByCpf(String cpf) {
        return findByPessoaCpf(cpf);
    }

    default Optional<ResponsavelEntity> findByCpfAndEscolaId(String cpf, UUID escolaId) {
        return findByPessoa_CpfAndPessoa_Escola_Id(cpf, escolaId);
    }

    default boolean existsByCpfAndIdNot(String cpf, UUID id) {
        return existsByPessoaCpfAndIdNot(cpf, id);
    }

    default boolean existsByCpfAndEscolaIdAndIdNot(String cpf, UUID escolaId, UUID id) {
        return existsByPessoa_CpfAndPessoa_Escola_IdAndIdNot(cpf, escolaId, id);
    }
}
