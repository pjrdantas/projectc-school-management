package br.com.escola.responsavel.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.responsavel.adapter.out.persistence.entity.ParentescoEntity;

public interface ParentescoJpaRepository extends JpaRepository<ParentescoEntity, UUID> {

    Optional<ParentescoEntity> findByCodigo(String codigo);
}
