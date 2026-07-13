package br.com.escola.planningaiservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.application.dto.CriarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiContentVersionJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiContentVersionJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiGeneratedContentJpaRepository;

@Service
public class PlanningAiContentVersionPersistenceService {

    private final PlanningAiGeneratedContentJpaRepository contentRepository;
    private final PlanningAiContentVersionJpaRepository versionRepository;

    public PlanningAiContentVersionPersistenceService(
            PlanningAiGeneratedContentJpaRepository contentRepository,
            PlanningAiContentVersionJpaRepository versionRepository) {
        this.contentRepository = contentRepository;
        this.versionRepository = versionRepository;
    }

    @Transactional
    public ConteudoIaVersaoResponse persistirCriacaoVersao(
            InternalRequestContext context,
            UUID conteudoId,
            CriarVersaoConteudoIaRequest request,
            ConteudoIaVersaoResponse response) {
        if (response == null) {
            return null;
        }

        PlanningAiGeneratedContentJpaEntity content = contentRepository.findByIdAndEscolaId(conteudoId, context.escolaId())
                .orElse(null);
        if (content == null) {
            return response;
        }

        PlanningAiContentVersionJpaEntity version = versionRepository.findById(response.id())
                .orElseGet(PlanningAiContentVersionJpaEntity::new);
        version.setId(response.id());
        version.setEscolaId(context.escolaId());
        version.setConteudoGerado(content);
        version.setAlteradoPor(context.usuarioId());
        version.setNumeroVersao(response.numeroVersao());
        version.setConteudo(response.conteudo());
        version.setMotivoAlteracao(response.motivoAlteracao());
        version.setCreatedAt(response.createdAt());
        versionRepository.save(version);

        content.setConteudo(response.conteudo());
        content.setVersao(response.numeroVersao());
        content.setUpdatedAt(response.createdAt());
        contentRepository.save(content);
        return response;
    }
}
