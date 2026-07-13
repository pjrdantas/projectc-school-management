package br.com.escola.planningaiservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiContentVersionJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiContentVersionJpaRepository;

@Service
public class PlanningAiContentVersionReadService {

    private final PlanningAiContentVersionJpaRepository versionRepository;

    public PlanningAiContentVersionReadService(PlanningAiContentVersionJpaRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    public List<ConteudoIaVersaoResponse> listarPorEscolaEConteudo(UUID escolaId, UUID conteudoId) {
        return versionRepository.findByEscolaIdAndConteudoGerado_IdOrderByNumeroVersaoAsc(escolaId, conteudoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ConteudoIaVersaoResponse toResponse(PlanningAiContentVersionJpaEntity entity) {
        return new ConteudoIaVersaoResponse(
                entity.getId(),
                entity.getConteudoGerado().getId(),
                entity.getNumeroVersao(),
                entity.getConteudo(),
                entity.getMotivoAlteracao(),
                entity.getCreatedAt());
    }
}
