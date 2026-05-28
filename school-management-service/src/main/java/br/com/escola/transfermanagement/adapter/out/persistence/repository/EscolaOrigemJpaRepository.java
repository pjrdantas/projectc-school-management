package br.com.escola.transfermanagement.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.transfermanagement.adapter.out.persistence.entity.EscolaOrigemEntity;

public interface EscolaOrigemJpaRepository extends JpaRepository<EscolaOrigemEntity, UUID> {
}
