package br.com.escola.historico.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.historico.adapter.out.persistence.entity.DisciplinaEntity;

public interface DisciplinaJpaRepository extends JpaRepository<DisciplinaEntity, UUID> {
}
