package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoVersaoEntity;

public interface PlanejamentoIAConteudoVersaoJpaRepository
        extends JpaRepository<PlanejamentoIAConteudoVersaoEntity, UUID> {

    List<PlanejamentoIAConteudoVersaoEntity> findByPlanejamentoIAConteudoGeradoId(
            UUID planejamentoIAConteudoGeradoId);

    List<PlanejamentoIAConteudoVersaoEntity> findByPlanejamentoIAConteudoGerado_IdAndPlanejamentoIAConteudoGerado_PlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID planejamentoIAConteudoGeradoId,
            UUID escolaId);

    Optional<PlanejamentoIAConteudoVersaoEntity> findByPlanejamentoIAConteudoGeradoIdAndNumeroVersao(
            UUID planejamentoIAConteudoGeradoId,
            Integer numeroVersao);

    Optional<PlanejamentoIAConteudoVersaoEntity> findByPlanejamentoIAConteudoGerado_IdAndPlanejamentoIAConteudoGerado_PlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndNumeroVersao(
            UUID planejamentoIAConteudoGeradoId,
            UUID escolaId,
            Integer numeroVersao);

    Optional<PlanejamentoIAConteudoVersaoEntity> findFirstByPlanejamentoIAConteudoGeradoIdOrderByNumeroVersaoDesc(
            UUID planejamentoIAConteudoGeradoId);

    Optional<PlanejamentoIAConteudoVersaoEntity> findFirstByPlanejamentoIAConteudoGerado_IdAndPlanejamentoIAConteudoGerado_PlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdOrderByNumeroVersaoDesc(
            UUID planejamentoIAConteudoGeradoId,
            UUID escolaId);
}
