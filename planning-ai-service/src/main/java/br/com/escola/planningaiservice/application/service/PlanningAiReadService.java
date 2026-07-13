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
import br.com.escola.planningaiservice.application.port.in.PlanningAiReadUseCase;
import br.com.escola.planningaiservice.application.port.out.PlanningAiReadPort;

@Service
public class PlanningAiReadService implements PlanningAiReadUseCase {

    private final PlanningAiReadPort planningAiReadPort;
    private final PlanningAiGenerationPersistenceService planningAiGenerationPersistenceService;
    private final PlanningAiInteractionReadService planningAiInteractionReadService;
    private final PlanningAiContentReadService planningAiContentReadService;
    private final PlanningAiContentVersionReadService planningAiContentVersionReadService;
    private final PlanningAiLibraryPublicationPersistenceService planningAiLibraryPublicationPersistenceService;
    private final PlanningAiLibraryReadService planningAiLibraryReadService;

    public PlanningAiReadService(
            PlanningAiReadPort planningAiReadPort,
            PlanningAiGenerationPersistenceService planningAiGenerationPersistenceService,
            PlanningAiInteractionReadService planningAiInteractionReadService,
            PlanningAiContentReadService planningAiContentReadService,
            PlanningAiContentVersionReadService planningAiContentVersionReadService,
            PlanningAiLibraryPublicationPersistenceService planningAiLibraryPublicationPersistenceService,
            PlanningAiLibraryReadService planningAiLibraryReadService) {
        this.planningAiReadPort = planningAiReadPort;
        this.planningAiGenerationPersistenceService = planningAiGenerationPersistenceService;
        this.planningAiInteractionReadService = planningAiInteractionReadService;
        this.planningAiContentReadService = planningAiContentReadService;
        this.planningAiContentVersionReadService = planningAiContentVersionReadService;
        this.planningAiLibraryPublicationPersistenceService = planningAiLibraryPublicationPersistenceService;
        this.planningAiLibraryReadService = planningAiLibraryReadService;
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
        return planningAiReadPort.listarBiblioteca(
                authorization,
                context,
                professorId,
                disciplinaId,
                tipoConteudo,
                tema);
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
        return planningAiReadPort.listarInteracoes(authorization, context, planejamentoId);
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
        return planningAiReadPort.listarConteudos(authorization, context, planejamentoId);
    }

    @Override
    public ConteudoIaResponse buscarConteudo(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        return planningAiContentReadService.buscarPorIdEEscola(conteudoId, context.escolaId())
                .orElseGet(() -> planningAiReadPort.buscarConteudo(
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
        return planningAiReadPort.listarVersoes(
                authorization,
                context,
                conteudoId);
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
        return planningAiReadPort.criarVersao(
                authorization,
                context,
                conteudoId,
                request);
    }

    @Override
    public ConteudoIaResponse aprovarVersao(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId,
            AprovarVersaoConteudoIaRequest request) {
        return planningAiReadPort.aprovarVersao(
                authorization,
                context,
                conteudoId,
                request);
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
