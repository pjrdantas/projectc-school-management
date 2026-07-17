package br.com.escola.planningaiservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.planningaiservice.application.dto.PlanejamentoIaInteracaoResponse;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.InteracaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.InteracaoJpaRepository;

@Service
public class InteracaoLeituraService {

    private final InteracaoJpaRepository interactionRepository;

    public InteracaoLeituraService(InteracaoJpaRepository interactionRepository) {
        this.interactionRepository = interactionRepository;
    }

    public List<PlanejamentoIaInteracaoResponse> listarPorEscolaEPlanejamento(
            UUID escolaId,
            UUID planejamentoId) {
        return interactionRepository.findByEscolaIdAndPlanejamentoBimestralIdOrderByCreatedAtAsc(escolaId, planejamentoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PlanejamentoIaInteracaoResponse toResponse(InteracaoJpaEntity entity) {
        return new PlanejamentoIaInteracaoResponse(
                entity.getId(),
                entity.getPlanejamentoBimestralId(),
                entity.getEscolaId(),
                entity.getEscolaNome(),
                entity.getPromptProfessor(),
                entity.getRespostaIa(),
                entity.getModeloIa(),
                entity.getTokensEntrada(),
                entity.getTokensSaida(),
                entity.getCustoEstimado(),
                entity.getCreatedAt());
    }
}

