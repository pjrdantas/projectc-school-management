package br.com.escola.historico.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolarPendencia;

public interface HistoricoEscolarPendenciaJpaRepository extends JpaRepository<HistoricoEscolarPendencia, UUID> {

    long countByHistoricoEscolar_IdAndResolvidaFalse(UUID historicoEscolarId);
}
