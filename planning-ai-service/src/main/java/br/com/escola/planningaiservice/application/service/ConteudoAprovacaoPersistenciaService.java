package br.com.escola.planningaiservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.AprovarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoVersaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoGeradoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoVersaoJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoGeradoJpaRepository;

@Service
public class ConteudoAprovacaoPersistenciaService {

    private final ConteudoGeradoJpaRepository contentRepository;
    private final ConteudoVersaoJpaRepository versionRepository;

    public ConteudoAprovacaoPersistenciaService(
            ConteudoGeradoJpaRepository contentRepository,
            ConteudoVersaoJpaRepository versionRepository) {
        this.contentRepository = contentRepository;
        this.versionRepository = versionRepository;
    }

    @Transactional
    public ConteudoIaResponse persistirAprovacao(
            InternalRequestContext context,
            UUID conteudoId,
            AprovarVersaoConteudoIaRequest request,
            ConteudoIaResponse response) {
        if (response == null) {
            return null;
        }

        ConteudoGeradoJpaEntity content = contentRepository.findByIdAndEscolaId(conteudoId, context.escolaId())
                .orElse(null);
        if (content == null) {
            return response;
        }

        content.setConteudo(response.conteudo());
        content.setVersao(response.versao());
        content.setHashConteudo(response.hashConteudo());
        content.setEscolaNome(response.escolaNome());
        content.setAprovadoPeloProfessor(Boolean.TRUE.equals(response.aprovadoPeloProfessor()));
        content.setReutilizavel(Boolean.TRUE.equals(response.reutilizavel()));
        content.setAtivo(Boolean.TRUE.equals(response.ativo()));
        content.setStatus(response.status());
        content.setUpdatedAt(response.updatedAt());
        contentRepository.save(content);

        if (request.numeroVersao() != null) {
            versionRepository.findByEscolaIdAndConteudoGerado_IdOrderByNumeroVersaoAsc(context.escolaId(), conteudoId)
                    .stream()
                    .filter(version -> request.numeroVersao().equals(version.getNumeroVersao()))
                    .findFirst()
                    .ifPresent(version -> atualizarVersao(version, context, response));
        }

        return response;
    }

    private void atualizarVersao(
            ConteudoVersaoJpaEntity version,
            InternalRequestContext context,
            ConteudoIaResponse response) {
        version.setAlteradoPor(context.usuarioId());
        version.setConteudo(response.conteudo());
        versionRepository.save(version);
    }
}

