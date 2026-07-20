package br.com.escola.planningaiservice.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.AprovarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoVersaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoGeradoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoVersaoJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoGeradoJpaRepository;

@Service
public class ConteudoAprovacaoPersistenciaService {

    private final ConteudoGeradoJpaRepository contentRepository;
    private final ConteudoVersaoJpaRepository versionRepository;
    private final DescritorService descriptorService;

    public ConteudoAprovacaoPersistenciaService(
            ConteudoGeradoJpaRepository contentRepository,
            ConteudoVersaoJpaRepository versionRepository,
            DescritorService descriptorService) {
        this.contentRepository = contentRepository;
        this.versionRepository = versionRepository;
        this.descriptorService = descriptorService;
    }

    @Transactional
    public ConteudoIaResponse aprovarVersao(
            InternalRequestContext context,
            UUID conteudoId,
            AprovarVersaoConteudoIaRequest request) {
        ConteudoGeradoJpaEntity content = contentRepository.findByIdAndEscolaId(conteudoId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteudo IA nao encontrado"));

        ConteudoVersaoJpaEntity version = versionRepository.findByEscolaIdAndConteudoGerado_IdOrderByNumeroVersaoAsc(
                context.escolaId(),
                conteudoId)
                .stream()
                .filter(entry -> request.numeroVersao().equals(entry.getNumeroVersao()))
                .findFirst()
                .orElseGet(() -> validarVersaoAtual(content, request.numeroVersao()));

        LocalDateTime now = LocalDateTime.now();
        content.setConteudo(version.getConteudo());
        content.setVersao(version.getNumeroVersao());
        content.setHashConteudo(hashConteudo(version.getConteudo()));
        content.setAprovadoPeloProfessor(true);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("APROVADO");
        content.setUpdatedAt(now);
        contentRepository.save(content);

        version.setAlteradoPor(context.usuarioId());
        version.setConteudo(content.getConteudo());
        versionRepository.save(version);

        return new ConteudoIaResponse(
                content.getId(),
                content.getPlanejamentoBimestralId(),
                content.getInteracao() == null ? null : content.getInteracao().getId(),
                content.getEscolaId(),
                content.getEscolaNome(),
                content.getTitulo(),
                content.getConteudo(),
                content.getVersao(),
                content.getHashConteudo(),
                content.isAprovadoPeloProfessor(),
                content.isReutilizavel(),
                content.isAtivo(),
                content.getStatus(),
                descriptorService.statusDescricao(content.getStatus()),
                content.getTipoConteudo(),
                descriptorService.tipoConteudoDescricao(content.getTipoConteudo()),
                content.getCreatedAt(),
                content.getUpdatedAt());
    }

    private ConteudoVersaoJpaEntity validarVersaoAtual(ConteudoGeradoJpaEntity content, Integer numeroVersao) {
        if (numeroVersao.equals(content.getVersao())) {
            ConteudoVersaoJpaEntity version = new ConteudoVersaoJpaEntity();
            version.setId(UUID.randomUUID());
            version.setEscolaId(content.getEscolaId());
            version.setConteudoGerado(content);
            version.setNumeroVersao(content.getVersao());
            version.setConteudo(content.getConteudo());
            version.setMotivoAlteracao("Aprovacao da versao atual");
            version.setCreatedAt(LocalDateTime.now());
            return versionRepository.save(version);
        }
        throw new RecursoNaoEncontradoException("Versao de conteudo IA nao encontrada");
    }

    private String hashConteudo(String conteudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(conteudo.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponivel", exception);
        }
    }
}

