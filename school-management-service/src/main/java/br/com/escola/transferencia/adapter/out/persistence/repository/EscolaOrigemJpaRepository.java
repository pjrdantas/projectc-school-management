package br.com.escola.transferencia.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;

public interface EscolaOrigemJpaRepository extends JpaRepository<EscolaEntity, UUID> {
}
