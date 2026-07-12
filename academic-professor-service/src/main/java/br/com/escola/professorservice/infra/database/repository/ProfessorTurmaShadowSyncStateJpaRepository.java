package br.com.escola.professorservice.infra.database.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professorservice.infra.database.entity.ProfessorTurmaShadowSyncStateJpaEntity;

public interface ProfessorTurmaShadowSyncStateJpaRepository
        extends JpaRepository<ProfessorTurmaShadowSyncStateJpaEntity, UUID> {
}
