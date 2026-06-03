package br.com.escola.transferencia.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.transferencia.adapter.out.persistence.entity.EscolaOrigemEntity;

public interface EscolaOrigemJpaRepository extends JpaRepository<EscolaOrigemEntity, UUID> {
}
