package br.com.escola.catalogo.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;

public interface PeriodoLetivoJpaRepository extends JpaRepository<PeriodoLetivoEntity, UUID> {
}
