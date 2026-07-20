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

@Service
public class PlanejamentoLeituraService implements PlanejamentoLeituraUseCase {

    private final GeracaoPersistenciaService planningAiGenerationPersistenceService;
    private final InteracaoLeituraService planningAiInteractionReadService;
    private final ConteudoLeituraService planningAiContentReadService;
    private final ConteudoVersaoLeituraService planningAiContentVersionReadService;
    private final BibliotecaPublicacaoPersistenciaService planningAiLibraryPublicationPersistenceService;
    private final BibliotecaLeituraService planningAiLibraryReadService;
    private final ConteudoVersaoPersistenciaService planningAiContentVersionPersistenceService;
    private final ConteudoAprovacaoPersistenciaService planningAiContentApprovalPersistenceService;

    public PlanejamentoLeituraService(
            GeracaoPersistenciaService planningAiGenerationPersistenceService,
            InteracaoLeituraService planningAiInteractionReadService,
            ConteudoLeituraService planningAiContentReadService,
            ConteudoVersaoLeituraService planningAiContentVersionReadService,
            BibliotecaPublicacaoPersistenciaService planningAiLibraryPublicationPersistenceService,
            BibliotecaLeituraService planningAiLibraryReadService,
            ConteudoVersaoPersistenciaService planningAiContentVersionPersistenceService,
            ConteudoAprovacaoPersistenciaService planningAiContentApprovalPersistenceService) {
        this.planningAiGenerationPersistenceService = planningAiGenerationPersistenceService;
        this.planningAiInteractionReadService = planningAiInteractionReadService;
        this.planningAiContentReadService = planningAiContentReadService;
        this.planningAiContentVersionReadService = planningAiContentVersionReadService;
        this.planningAiLibraryPublicationPersistenceService = planningAiLibraryPublicationPersistenceService;
        this.planningAiLibraryReadService = planningAiLibraryReadService;
        this.planningAiContentVersionPersistenceService = planningAiContentVersionPersistenceService;
        this.planningAiContentApprovalPersistenceService = planningAiContentApprovalPersistenceService;
    }

    @Override
    public List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        return planningAiLibraryReadService.listar(context.escolaId(), professorId, disciplinaId, tipoConteudo, tema);
    }

    @Override
    public List<PlanejamentoIaInteracaoResponse> listarInteracoes(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId) {
        return planningAiInteractionReadService.listarPorEscolaEPlanejamento(context.escolaId(), planejamentoId);
    }

    @Override
    public List<ConteudoIaResponse> listarConteudos(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId) {
        return planningAiContentReadService.listarPorEscolaEPlanejamento(context.escolaId(), planejamentoId);
    }

    @Override
    public ConteudoIaResponse buscarConteudo(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        return planningAiContentReadService.buscarPorIdEEscola(conteudoId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteudo IA nao encontrado"));
    }

    @Override
    public List<ConteudoIaVersaoResponse> listarVersoes(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        List<ConteudoIaVersaoResponse> localVersions = planningAiContentVersionReadService
                .listarPorEscolaEConteudo(context.escolaId(), conteudoId);
        if (localVersions.isEmpty()
                && planningAiContentReadService.buscarPorIdEEscola(conteudoId, context.escolaId()).isEmpty()) {
            throw new RecursoNaoEncontradoException("Conteudo IA nao encontrado");
        }
        return localVersions;
    }

    @Override
    public ConteudoIaResponse gerarConteudo(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId,
            GerarConteudoIaRequest request) {
        return planningAiGenerationPersistenceService.gerarConteudo(context, planejamentoId, request);
    }

    @Override
    public ConteudoIaVersaoResponse criarVersao(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId,
            CriarVersaoConteudoIaRequest request) {
        return planningAiContentVersionPersistenceService.criarVersao(context, conteudoId, request);
    }

    @Override
    public ConteudoIaResponse aprovarVersao(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId,
            AprovarVersaoConteudoIaRequest request) {
        ConteudoIaResponse response = planningAiContentApprovalPersistenceService.aprovarVersao(context, conteudoId, request);
        if (Boolean.TRUE.equals(request.publicarBiblioteca())) {
            planningAiLibraryPublicationPersistenceService.publicarBiblioteca(context, conteudoId);
        }
        return response;
    }

    @Override
    public BibliotecaConteudoPedagogicoResponse publicarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        return planningAiLibraryPublicationPersistenceService.publicarBiblioteca(context, conteudoId);
    }
}

