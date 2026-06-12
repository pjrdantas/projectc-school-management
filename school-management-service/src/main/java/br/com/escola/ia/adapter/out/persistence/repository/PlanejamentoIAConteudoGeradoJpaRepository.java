package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoGeradoEntity;

public interface PlanejamentoIAConteudoGeradoJpaRepository
        extends JpaRepository<PlanejamentoIAConteudoGeradoEntity, UUID> {

    List<PlanejamentoIAConteudoGeradoEntity> findByPlanejamentoBimestralId(UUID planejamentoBimestralId);

    List<PlanejamentoIAConteudoGeradoEntity> findByPlanejamentoBimestral_IdAndPlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID planejamentoBimestralId,
            UUID escolaId);

    Optional<PlanejamentoIAConteudoGeradoEntity> findByIdAndPlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID id,
            UUID escolaId);

    boolean existsByIdAndPlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID id,
            UUID escolaId);
}
