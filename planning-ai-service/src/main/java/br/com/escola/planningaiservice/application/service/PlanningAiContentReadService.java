package br.com.escola.planningaiservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiGeneratedContentJpaRepository;

@Service
public class PlanningAiContentReadService {

    private final PlanningAiGeneratedContentJpaRepository contentRepository;

    public PlanningAiContentReadService(PlanningAiGeneratedContentJpaRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public List<ConteudoIaResponse> listarPorEscolaEPlanejamento(UUID escolaId, UUID planejamentoId) {
        return contentRepository.findByEscolaIdAndPlanejamentoBimestralIdOrderByCreatedAtAsc(escolaId, planejamentoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ConteudoIaResponse toResponse(PlanningAiGeneratedContentJpaEntity entity) {
        return new ConteudoIaResponse(
                entity.getId(),
                entity.getPlanejamentoBimestralId(),
                entity.getInteracao() == null ? null : entity.getInteracao().getId(),
                entity.getEscolaId(),
                null,
                entity.getTitulo(),
                entity.getConteudo(),
                entity.getVersao(),
                entity.getHashConteudo(),
                entity.isAprovadoPeloProfessor(),
                entity.isReutilizavel(),
                entity.isAtivo(),
                entity.getStatus(),
                null,
                entity.getTipoConteudo(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
