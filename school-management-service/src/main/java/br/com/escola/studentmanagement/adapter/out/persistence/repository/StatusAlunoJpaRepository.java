package br.com.escola.studentmanagement.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.studentmanagement.adapter.out.persistence.entity.StatusAlunoEntity;

public interface StatusAlunoJpaRepository extends JpaRepository<StatusAlunoEntity, UUID> {

    Optional<StatusAlunoEntity> findByCodigo(String codigo);
}
