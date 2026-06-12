package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAInteracaoEntity;

public interface PlanejamentoIAInteracaoJpaRepository extends JpaRepository<PlanejamentoIAInteracaoEntity, UUID> {

    List<PlanejamentoIAInteracaoEntity> findByPlanejamentoBimestralId(UUID planejamentoBimestralId);

    List<PlanejamentoIAInteracaoEntity> findByPlanejamentoBimestral_IdAndPlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID planejamentoBimestralId,
            UUID escolaId);
}
