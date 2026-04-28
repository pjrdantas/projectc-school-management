package br.com.escola.studentmanagement.adapter.out.persistence.repository;

import java.util.UUID;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.studentmanagement.adapter.out.persistence.entity.AlunoEntity;

public interface AlunoJpaRepository extends JpaRepository<AlunoEntity, UUID> {

    Optional<AlunoEntity> findByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, UUID id);
}
