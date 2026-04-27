package br.com.escola.academiccatalog.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.PeriodoLetivoEntity;

public interface PeriodoLetivoJpaRepository extends JpaRepository<PeriodoLetivoEntity, Long> {
}
