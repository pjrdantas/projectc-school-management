package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoVersaoEntity;

public interface PlanejamentoIAConteudoVersaoJpaRepository
        extends JpaRepository<PlanejamentoIAConteudoVersaoEntity, UUID> {

    List<PlanejamentoIAConteudoVersaoEntity> findByPlanejamentoIAConteudoGeradoId(
            UUID planejamentoIAConteudoGeradoId);
}
