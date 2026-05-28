package br.com.escola.responsavelmanagement.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.responsavelmanagement.adapter.out.persistence.entity.ResponsavelEntity;

public interface ResponsavelJpaRepository extends JpaRepository<ResponsavelEntity, UUID> {

    @EntityGraph(attributePaths = "pessoa")
    Optional<ResponsavelEntity> findByPessoaCpf(String cpf);

    boolean existsByPessoaCpfAndIdNot(String cpf, UUID id);

    default Optional<ResponsavelEntity> findByCpf(String cpf) {
        return findByPessoaCpf(cpf);
    }

    default boolean existsByCpfAndIdNot(String cpf, UUID id) {
        return existsByPessoaCpfAndIdNot(cpf, id);
    }
}
