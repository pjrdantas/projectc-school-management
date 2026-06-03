package br.com.escola.avaliacao.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.avaliacao.adapter.out.persistence.entity.AvaliacaoEntity;

public interface AvaliacaoJpaRepository extends JpaRepository<AvaliacaoEntity, UUID> {

    List<AvaliacaoEntity> findByProfessorTurmaDisciplinaId(UUID professorTurmaDisciplinaId);
}
