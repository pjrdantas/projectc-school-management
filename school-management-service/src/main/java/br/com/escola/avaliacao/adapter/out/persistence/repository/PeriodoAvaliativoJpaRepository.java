package br.com.escola.avaliacao.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.avaliacao.adapter.out.persistence.entity.PeriodoAvaliativoEntity;

public interface PeriodoAvaliativoJpaRepository extends JpaRepository<PeriodoAvaliativoEntity, UUID> {

    List<PeriodoAvaliativoEntity> findByPeriodoLetivoId(UUID periodoLetivoId);
}
