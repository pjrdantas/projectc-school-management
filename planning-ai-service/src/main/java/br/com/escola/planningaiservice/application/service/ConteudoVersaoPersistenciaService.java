package br.com.escola.planningaiservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.application.dto.CriarVersaoConteudoIaRequest;
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
    public ConteudoIaVersaoResponse persistirCriacaoVersao(
            InternalRequestContext context,
            UUID conteudoId,
            CriarVersaoConteudoIaRequest request,
            ConteudoIaVersaoResponse response) {
        if (response == null) {
            return null;
        }

        ConteudoGeradoJpaEntity content = contentRepository.findByIdAndEscolaId(conteudoId, context.escolaId())
                .orElse(null);
        if (content == null) {
            return response;
        }

        ConteudoVersaoJpaEntity version = versionRepository.findById(response.id())
                .orElseGet(ConteudoVersaoJpaEntity::new);
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

