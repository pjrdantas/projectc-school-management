package br.com.escola.planningaiservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.application.dto.GerarConteudoIaRequest;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiInteractionJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiGeneratedContentJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiInteractionJpaRepository;

@Service
public class PlanningAiGenerationPersistenceService {

    private final PlanningAiInteractionJpaRepository interactionRepository;
    private final PlanningAiGeneratedContentJpaRepository contentRepository;

    public PlanningAiGenerationPersistenceService(
            PlanningAiInteractionJpaRepository interactionRepository,
            PlanningAiGeneratedContentJpaRepository contentRepository) {
        this.interactionRepository = interactionRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional
    public ConteudoIaResponse persistirGeracao(
            InternalRequestContext context,
            GerarConteudoIaRequest request,
            ConteudoIaResponse response) {
        if (response == null) {
            return null;
        }

        PlanningAiInteractionJpaEntity interaction = upsertInteraction(context, request, response);
        upsertGeneratedContent(context, request, response, interaction);
        return response;
    }

    private PlanningAiInteractionJpaEntity upsertInteraction(
            InternalRequestContext context,
            GerarConteudoIaRequest request,
            ConteudoIaResponse response) {
        if (response.interacaoId() == null) {
            return null;
        }

        PlanningAiInteractionJpaEntity interaction = interactionRepository.findById(response.interacaoId())
                .orElseGet(PlanningAiInteractionJpaEntity::new);
        interaction.setId(response.interacaoId());
        interaction.setEscolaId(context.escolaId());
        interaction.setEscolaNome(response.escolaNome());
        interaction.setPlanejamentoBimestralId(response.planejamentoBimestralId());
        interaction.setUsuarioId(context.usuarioId());
        interaction.setPromptProfessor(request.promptProfessor());
        interaction.setRespostaIa(response.conteudo());
        interaction.setModeloIa(interaction.getModeloIa());
        interaction.setTokensEntrada(interaction.getTokensEntrada());
        interaction.setTokensSaida(interaction.getTokensSaida());
        interaction.setCustoEstimado(interaction.getCustoEstimado());
        interaction.setCreatedAt(response.createdAt());
        return interactionRepository.save(interaction);
    }

    private void upsertGeneratedContent(
            InternalRequestContext context,
            GerarConteudoIaRequest request,
            ConteudoIaResponse response,
            PlanningAiInteractionJpaEntity interaction) {
        PlanningAiGeneratedContentJpaEntity content = contentRepository.findById(response.id())
                .orElseGet(PlanningAiGeneratedContentJpaEntity::new);
        content.setId(response.id());
        content.setEscolaId(context.escolaId());
        content.setEscolaNome(response.escolaNome());
        content.setPlanejamentoBimestralId(response.planejamentoBimestralId());
        content.setInteracao(interaction);
        content.setTitulo(response.titulo() != null ? response.titulo() : request.titulo());
        content.setConteudo(response.conteudo());
        content.setVersao(response.versao());
        content.setHashConteudo(response.hashConteudo());
        content.setAprovadoPeloProfessor(Boolean.TRUE.equals(response.aprovadoPeloProfessor()));
        content.setReutilizavel(Boolean.TRUE.equals(response.reutilizavel()));
        content.setAtivo(Boolean.TRUE.equals(response.ativo()));
        content.setStatus(response.status());
        content.setTipoConteudo(response.tipoConteudo() != null ? response.tipoConteudo() : request.tipoConteudo());
        content.setCreatedAt(response.createdAt());
        content.setUpdatedAt(response.updatedAt());
        contentRepository.save(content);
    }
}
