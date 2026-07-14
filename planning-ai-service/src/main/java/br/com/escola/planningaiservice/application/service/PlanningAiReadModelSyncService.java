package br.com.escola.planningaiservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.application.dto.PlanejamentoIaInteracaoResponse;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PedagogicalContentLibraryJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiContentVersionJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiInteractionJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PedagogicalContentLibraryJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiContentVersionJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiGeneratedContentJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiInteractionJpaRepository;

@Service
public class PlanningAiReadModelSyncService {

    private final PlanningAiInteractionJpaRepository interactionRepository;
    private final PlanningAiGeneratedContentJpaRepository contentRepository;
    private final PlanningAiContentVersionJpaRepository versionRepository;
    private final PedagogicalContentLibraryJpaRepository libraryRepository;

    public PlanningAiReadModelSyncService(
            PlanningAiInteractionJpaRepository interactionRepository,
            PlanningAiGeneratedContentJpaRepository contentRepository,
            PlanningAiContentVersionJpaRepository versionRepository,
            PedagogicalContentLibraryJpaRepository libraryRepository) {
        this.interactionRepository = interactionRepository;
        this.contentRepository = contentRepository;
        this.versionRepository = versionRepository;
        this.libraryRepository = libraryRepository;
    }

    @Transactional
    public List<PlanejamentoIaInteracaoResponse> syncInteractions(
            InternalRequestContext context,
            List<PlanejamentoIaInteracaoResponse> responses) {
        responses.forEach(response -> upsertInteraction(context, response));
        return responses;
    }

    @Transactional
    public List<ConteudoIaResponse> syncContents(
            InternalRequestContext context,
            List<ConteudoIaResponse> responses) {
        responses.forEach(response -> upsertContent(context, response));
        return responses;
    }

    @Transactional
    public ConteudoIaResponse syncContent(
            InternalRequestContext context,
            ConteudoIaResponse response) {
        if (response == null) {
            return null;
        }
        upsertContent(context, response);
        return response;
    }

    @Transactional
    public List<ConteudoIaVersaoResponse> syncVersions(
            InternalRequestContext context,
            List<ConteudoIaVersaoResponse> responses) {
        responses.forEach(response -> upsertVersion(context, response));
        return responses;
    }

    @Transactional
    public List<BibliotecaConteudoPedagogicoResponse> syncLibrary(
            InternalRequestContext context,
            List<BibliotecaConteudoPedagogicoResponse> responses) {
        responses.forEach(response -> upsertLibraryEntry(context, response));
        return responses;
    }

    private PlanningAiInteractionJpaEntity upsertInteraction(
            InternalRequestContext context,
            PlanejamentoIaInteracaoResponse response) {
        PlanningAiInteractionJpaEntity interaction = interactionRepository.findById(response.id())
                .orElseGet(PlanningAiInteractionJpaEntity::new);
        interaction.setId(response.id());
        interaction.setEscolaId(context.escolaId());
        interaction.setPlanejamentoBimestralId(response.planejamentoBimestralId());
        interaction.setPromptProfessor(response.promptProfessor());
        interaction.setRespostaIa(response.respostaIA());
        interaction.setModeloIa(response.modeloIA());
        interaction.setTokensEntrada(response.tokensEntrada());
        interaction.setTokensSaida(response.tokensSaida());
        interaction.setCustoEstimado(response.custoEstimado());
        interaction.setCreatedAt(response.createdAt());
        return interactionRepository.save(interaction);
    }

    private PlanningAiGeneratedContentJpaEntity upsertContent(
            InternalRequestContext context,
            ConteudoIaResponse response) {
        PlanningAiGeneratedContentJpaEntity content = contentRepository.findById(response.id())
                .orElseGet(PlanningAiGeneratedContentJpaEntity::new);
        content.setId(response.id());
        content.setEscolaId(context.escolaId());
        content.setPlanejamentoBimestralId(response.planejamentoBimestralId());
        content.setInteracao(resolveInteraction(response.interacaoId()));
        content.setTitulo(response.titulo());
        content.setConteudo(response.conteudo());
        content.setVersao(response.versao());
        content.setHashConteudo(response.hashConteudo());
        content.setAprovadoPeloProfessor(Boolean.TRUE.equals(response.aprovadoPeloProfessor()));
        content.setReutilizavel(Boolean.TRUE.equals(response.reutilizavel()));
        content.setAtivo(Boolean.TRUE.equals(response.ativo()));
        content.setStatus(response.status());
        content.setTipoConteudo(response.tipoConteudo());
        content.setCreatedAt(response.createdAt());
        content.setUpdatedAt(response.updatedAt());
        return contentRepository.save(content);
    }

    private void upsertVersion(
            InternalRequestContext context,
            ConteudoIaVersaoResponse response) {
        PlanningAiGeneratedContentJpaEntity content = contentRepository.findByIdAndEscolaId(
                response.conteudoGeradoId(),
                context.escolaId()).orElse(null);
        if (content == null) {
            return;
        }

        PlanningAiContentVersionJpaEntity version = versionRepository.findById(response.id())
                .orElseGet(PlanningAiContentVersionJpaEntity::new);
        version.setId(response.id());
        version.setEscolaId(context.escolaId());
        version.setConteudoGerado(content);
        version.setNumeroVersao(response.numeroVersao());
        version.setConteudo(response.conteudo());
        version.setMotivoAlteracao(response.motivoAlteracao());
        version.setCreatedAt(response.createdAt());
        versionRepository.save(version);
    }

    private void upsertLibraryEntry(
            InternalRequestContext context,
            BibliotecaConteudoPedagogicoResponse response) {
        PedagogicalContentLibraryJpaEntity library = libraryRepository.findById(response.id())
                .orElseGet(PedagogicalContentLibraryJpaEntity::new);
        library.setId(response.id());
        library.setEscolaId(context.escolaId());
        library.setConteudoOrigem(null);
        library.setProfessorId(response.professorId());
        library.setDisciplinaId(response.disciplinaId());
        library.setTipoConteudo(response.tipoConteudo());
        library.setTitulo(response.titulo());
        library.setTema(response.tema());
        library.setConteudo(response.conteudo());
        library.setOrigem(response.origem());
        library.setReutilizavel(Boolean.TRUE.equals(response.reutilizavel()));
        library.setAtivo(Boolean.TRUE.equals(response.ativo()));
        library.setCreatedAt(response.createdAt());
        library.setUpdatedAt(response.updatedAt());
        libraryRepository.save(library);
    }

    private PlanningAiInteractionJpaEntity resolveInteraction(java.util.UUID interactionId) {
        if (interactionId == null) {
            return null;
        }
        return interactionRepository.findById(interactionId).orElse(null);
    }
}
