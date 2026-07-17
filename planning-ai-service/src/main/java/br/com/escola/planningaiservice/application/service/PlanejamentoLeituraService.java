package br.com.escola.planningaiservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.AprovarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.application.dto.CriarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.GerarConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoIaInteracaoResponse;
import br.com.escola.planningaiservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.planningaiservice.application.port.in.PlanejamentoLeituraUseCase;
import br.com.escola.planningaiservice.application.port.out.PlanejamentoLeituraPort;

@Service
public class PlanejamentoLeituraService implements PlanejamentoLeituraUseCase {

    private final PlanejamentoLeituraPort planningAiReadPort;
    private final GeracaoPersistenciaService planningAiGenerationPersistenceService;
    private final InteracaoLeituraService planningAiInteractionReadService;
    private final ConteudoLeituraService planningAiContentReadService;
    private final ConteudoVersaoLeituraService planningAiContentVersionReadService;
    private final BibliotecaPublicacaoPersistenciaService planningAiLibraryPublicationPersistenceService;
    private final BibliotecaLeituraService planningAiLibraryReadService;
    private final ConteudoVersaoPersistenciaService planningAiContentVersionPersistenceService;
    private final ConteudoAprovacaoPersistenciaService planningAiContentApprovalPersistenceService;
    private final LeituraModeloSyncService planningAiReadModelSyncService;
    private final LeituraModeloSyncStateService planningAiReadModelSyncStateService;

    public PlanejamentoLeituraService(
            PlanejamentoLeituraPort planningAiReadPort,
            GeracaoPersistenciaService planningAiGenerationPersistenceService,
            InteracaoLeituraService planningAiInteractionReadService,
            ConteudoLeituraService planningAiContentReadService,
            ConteudoVersaoLeituraService planningAiContentVersionReadService,
            BibliotecaPublicacaoPersistenciaService planningAiLibraryPublicationPersistenceService,
            BibliotecaLeituraService planningAiLibraryReadService,
            ConteudoVersaoPersistenciaService planningAiContentVersionPersistenceService,
            ConteudoAprovacaoPersistenciaService planningAiContentApprovalPersistenceService,
            LeituraModeloSyncService planningAiReadModelSyncService,
            LeituraModeloSyncStateService planningAiReadModelSyncStateService) {
        this.planningAiReadPort = planningAiReadPort;
        this.planningAiGenerationPersistenceService = planningAiGenerationPersistenceService;
        this.planningAiInteractionReadService = planningAiInteractionReadService;
        this.planningAiContentReadService = planningAiContentReadService;
        this.planningAiContentVersionReadService = planningAiContentVersionReadService;
        this.planningAiLibraryPublicationPersistenceService = planningAiLibraryPublicationPersistenceService;
        this.planningAiLibraryReadService = planningAiLibraryReadService;
        this.planningAiContentVersionPersistenceService = planningAiContentVersionPersistenceService;
        this.planningAiContentApprovalPersistenceService = planningAiContentApprovalPersistenceService;
        this.planningAiReadModelSyncService = planningAiReadModelSyncService;
        this.planningAiReadModelSyncStateService = planningAiReadModelSyncStateService;
    }

    @Override
    public List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        List<BibliotecaConteudoPedagogicoResponse> localLibrary = planningAiLibraryReadService
                .listar(context.escolaId(), professorId, disciplinaId, tipoConteudo, tema);
        if (!localLibrary.isEmpty()) {
            return localLibrary;
        }
        if (planningAiReadModelSyncStateService.librarySynced(context.escolaId(), professorId, disciplinaId, tipoConteudo, tema)) {
            return List.of();
        }
        List<BibliotecaConteudoPedagogicoResponse> response = planningAiReadModelSyncService.syncLibrary(
                context,
                planningAiReadPort.listarBiblioteca(
                        authorization,
                        context,
                        professorId,
                        disciplinaId,
                        tipoConteudo,
                        tema));
        planningAiReadModelSyncStateService.markLibrarySynced(
                context.escolaId(),
                professorId,
                disciplinaId,
                tipoConteudo,
                tema);
        return response;
    }

    @Override
    public List<PlanejamentoIaInteracaoResponse> listarInteracoes(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId) {
        List<PlanejamentoIaInteracaoResponse> localInteractions = planningAiInteractionReadService
                .listarPorEscolaEPlanejamento(context.escolaId(), planejamentoId);
        if (!localInteractions.isEmpty()) {
            return localInteractions;
        }
        if (planningAiReadModelSyncStateService.interactionsSynced(context.escolaId(), planejamentoId)) {
            return List.of();
        }
        List<PlanejamentoIaInteracaoResponse> response = planningAiReadModelSyncService.syncInteractions(
                context,
                planningAiReadPort.listarInteracoes(authorization, context, planejamentoId));
        planningAiReadModelSyncStateService.markInteractionsSynced(context.escolaId(), planejamentoId);
        return response;
    }

    @Override
    public List<ConteudoIaResponse> listarConteudos(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId) {
        List<ConteudoIaResponse> localContents = planningAiContentReadService
                .listarPorEscolaEPlanejamento(context.escolaId(), planejamentoId);
        if (!localContents.isEmpty()) {
            return localContents;
        }
        if (planningAiReadModelSyncStateService.contentsSynced(context.escolaId(), planejamentoId)) {
            return List.of();
        }
        List<ConteudoIaResponse> response = planningAiReadModelSyncService.syncContents(
                context,
                planningAiReadPort.listarConteudos(authorization, context, planejamentoId));
        planningAiReadModelSyncStateService.markContentsSynced(context.escolaId(), planejamentoId);
        return response;
    }

    @Override
    public ConteudoIaResponse buscarConteudo(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        return planningAiContentReadService.buscarPorIdEEscola(conteudoId, context.escolaId())
                .orElseGet(() -> buscarConteudoComFallbackControlado(
                        authorization,
                        context,
                        conteudoId));
    }

    @Override
    public List<ConteudoIaVersaoResponse> listarVersoes(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        List<ConteudoIaVersaoResponse> localVersions = planningAiContentVersionReadService
                .listarPorEscolaEConteudo(context.escolaId(), conteudoId);
        if (!localVersions.isEmpty()) {
            return localVersions;
        }
        if (planningAiReadModelSyncStateService.contentVersionsNotFound(context.escolaId(), conteudoId)) {
            throw new RecursoNaoEncontradoException(
                    "Consulta de versoes de conteudo de planejamento IA nao encontrada");
        }
        if (planningAiReadModelSyncStateService.versionsSynced(context.escolaId(), conteudoId)) {
            return List.of();
        }
        try {
            List<ConteudoIaVersaoResponse> response = planningAiReadModelSyncService.syncVersions(
                    context,
                    planningAiReadPort.listarVersoes(
                            authorization,
                            context,
                            conteudoId));
            planningAiReadModelSyncStateService.markVersionsSynced(context.escolaId(), conteudoId);
            return response;
        } catch (RecursoNaoEncontradoException exception) {
            planningAiReadModelSyncStateService.markContentVersionsNotFound(context.escolaId(), conteudoId);
            throw exception;
        }
    }

    private ConteudoIaResponse buscarConteudoComFallbackControlado(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        if (planningAiReadModelSyncStateService.contentNotFound(context.escolaId(), conteudoId)) {
            throw new RecursoNaoEncontradoException(
                    "Consulta de conteudo de planejamento IA nao encontrada");
        }
        try {
            return planningAiReadModelSyncService.syncContent(
                    context,
                    planningAiReadPort.buscarConteudo(
                            authorization,
                            context,
                            conteudoId));
        } catch (RecursoNaoEncontradoException exception) {
            planningAiReadModelSyncStateService.markContentNotFound(context.escolaId(), conteudoId);
            throw exception;
        }
    }

    @Override
    public ConteudoIaResponse gerarConteudo(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId,
            GerarConteudoIaRequest request) {
        ConteudoIaResponse response = planningAiReadPort.gerarConteudo(
                authorization,
                context,
                planejamentoId,
                request);
        return planningAiGenerationPersistenceService.persistirGeracao(
                context,
                request,
                response);
    }

    @Override
    public ConteudoIaVersaoResponse criarVersao(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId,
            CriarVersaoConteudoIaRequest request) {
        ConteudoIaVersaoResponse response = planningAiReadPort.criarVersao(
                authorization,
                context,
                conteudoId,
                request);
        return planningAiContentVersionPersistenceService.persistirCriacaoVersao(
                context,
                conteudoId,
                request,
                response);
    }

    @Override
    public ConteudoIaResponse aprovarVersao(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId,
            AprovarVersaoConteudoIaRequest request) {
        ConteudoIaResponse response = planningAiReadPort.aprovarVersao(
                authorization,
                context,
                conteudoId,
                request);
        return planningAiContentApprovalPersistenceService.persistirAprovacao(
                context,
                conteudoId,
                request,
                response);
    }

    @Override
    public BibliotecaConteudoPedagogicoResponse publicarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        BibliotecaConteudoPedagogicoResponse response = planningAiReadPort.publicarBiblioteca(
                authorization,
                context,
                conteudoId);
        return planningAiLibraryPublicationPersistenceService.persistirPublicacao(
                context,
                conteudoId,
                response);
    }
}

