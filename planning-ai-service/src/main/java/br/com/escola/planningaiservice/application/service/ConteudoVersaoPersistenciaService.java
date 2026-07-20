package br.com.escola.planningaiservice.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.application.dto.CriarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoVersaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoGeradoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoVersaoJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoGeradoJpaRepository;

@Service
public class ConteudoVersaoPersistenciaService {

    private final ConteudoGeradoJpaRepository contentRepository;
    private final ConteudoVersaoJpaRepository versionRepository;

    public ConteudoVersaoPersistenciaService(
            ConteudoGeradoJpaRepository contentRepository,
            ConteudoVersaoJpaRepository versionRepository) {
        this.contentRepository = contentRepository;
        this.versionRepository = versionRepository;
    }

    @Transactional
    public ConteudoIaVersaoResponse criarVersao(
            InternalRequestContext context,
            UUID conteudoId,
            CriarVersaoConteudoIaRequest request) {
        ConteudoGeradoJpaEntity content = contentRepository.findByIdAndEscolaId(conteudoId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteudo IA nao encontrado"));

        int proximaVersao = Math.max(content.getVersao(), obterUltimaVersao(content.getId(), context.escolaId())) + 1;
        LocalDateTime now = LocalDateTime.now();

        ConteudoVersaoJpaEntity version = new ConteudoVersaoJpaEntity();
        version.setId(UUID.randomUUID());
        version.setEscolaId(context.escolaId());
        version.setConteudoGerado(content);
        version.setAlteradoPor(context.usuarioId());
        version.setNumeroVersao(proximaVersao);
        version.setConteudo(request.conteudo());
        version.setMotivoAlteracao(request.motivoAlteracao());
        version.setCreatedAt(now);
        versionRepository.save(version);

        content.setConteudo(request.conteudo());
        content.setVersao(proximaVersao);
        content.setAprovadoPeloProfessor(false);
        content.setStatus("EM_EDICAO");
        content.setUpdatedAt(now);
        contentRepository.save(content);
        return new ConteudoIaVersaoResponse(
                version.getId(),
                content.getId(),
                version.getNumeroVersao(),
                version.getConteudo(),
                version.getMotivoAlteracao(),
                version.getCreatedAt());
    }

    private int obterUltimaVersao(UUID conteudoId, UUID escolaId) {
        return versionRepository.findByEscolaIdAndConteudoGerado_IdOrderByNumeroVersaoAsc(escolaId, conteudoId)
                .stream()
                .map(ConteudoVersaoJpaEntity::getNumeroVersao)
                .max(Integer::compareTo)
                .orElse(0);
    }
}

