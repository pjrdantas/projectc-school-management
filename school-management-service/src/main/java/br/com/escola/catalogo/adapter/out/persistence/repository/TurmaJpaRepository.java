package br.com.escola.catalogo.adapter.out.persistence.repository;

import java.util.UUID;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;

public interface TurmaJpaRepository extends JpaRepository<TurmaEntity, UUID> {

    Optional<TurmaEntity> findByCodigoAndPeriodoLetivo_Id(String codigo, UUID periodoLetivoId);
}
