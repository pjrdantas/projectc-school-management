package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.LeituraModeloSyncStateJpaEntity;

public interface LeituraModeloSyncStateJpaRepository
        extends JpaRepository<LeituraModeloSyncStateJpaEntity, String> {
}

