package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.BibliotecaConteudoJpaEntity;

public interface BibliotecaConteudoJpaRepository
        extends JpaRepository<BibliotecaConteudoJpaEntity, UUID> {

    List<BibliotecaConteudoJpaEntity> findByEscolaIdOrderByCreatedAtAsc(UUID escolaId);

    Optional<BibliotecaConteudoJpaEntity> findByEscolaIdAndConteudoOrigem_Id(UUID escolaId, UUID conteudoOrigemId);
}

