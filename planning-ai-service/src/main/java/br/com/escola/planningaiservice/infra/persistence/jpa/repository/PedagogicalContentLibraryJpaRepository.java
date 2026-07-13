package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PedagogicalContentLibraryJpaEntity;

public interface PedagogicalContentLibraryJpaRepository
        extends JpaRepository<PedagogicalContentLibraryJpaEntity, UUID> {
}
