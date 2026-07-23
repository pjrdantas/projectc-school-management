package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoVersaoJpaEntity;

public interface ConteudoVersaoJpaRepository extends JpaRepository<ConteudoVersaoJpaEntity, UUID> {

    List<ConteudoVersaoJpaEntity> findByEscolaIdAndConteudoGerado_IdOrderByNumeroVersaoAsc(
            UUID escolaId,
            UUID conteudoGeradoId);
}

