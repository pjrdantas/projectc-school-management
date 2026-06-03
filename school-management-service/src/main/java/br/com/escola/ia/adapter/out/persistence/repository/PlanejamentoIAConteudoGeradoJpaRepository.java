package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoGeradoEntity;

public interface PlanejamentoIAConteudoGeradoJpaRepository
        extends JpaRepository<PlanejamentoIAConteudoGeradoEntity, UUID> {

    List<PlanejamentoIAConteudoGeradoEntity> findByPlanejamentoBimestralId(UUID planejamentoBimestralId);
}
