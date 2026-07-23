package br.com.escola.planningaiservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoVersaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoVersaoJpaRepository;

@Service
public class ConteudoVersaoLeituraService {

    private final ConteudoVersaoJpaRepository versionRepository;

    public ConteudoVersaoLeituraService(ConteudoVersaoJpaRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    public List<ConteudoIaVersaoResponse> listarPorEscolaEConteudo(UUID escolaId, UUID conteudoId) {
        return versionRepository.findByEscolaIdAndConteudoGerado_IdOrderByNumeroVersaoAsc(escolaId, conteudoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ConteudoIaVersaoResponse toResponse(ConteudoVersaoJpaEntity entity) {
        return new ConteudoIaVersaoResponse(
                entity.getId(),
                entity.getConteudoGerado().getId(),
                entity.getNumeroVersao(),
                entity.getConteudo(),
                entity.getMotivoAlteracao(),
                entity.getCreatedAt());
    }
}

