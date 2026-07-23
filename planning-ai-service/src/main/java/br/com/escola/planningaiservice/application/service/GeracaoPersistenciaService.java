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
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.application.dto.GerarConteudoIaRequest;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoGeradoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.InteracaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoGeradoJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.InteracaoJpaRepository;

@Service
public class GeracaoPersistenciaService {

    private final InteracaoJpaRepository interactionRepository;
    private final ConteudoGeradoJpaRepository contentRepository;
    private final DescritorService descriptorService;

    public GeracaoPersistenciaService(
            InteracaoJpaRepository interactionRepository,
            ConteudoGeradoJpaRepository contentRepository,
            DescritorService descriptorService) {
        this.interactionRepository = interactionRepository;
        this.contentRepository = contentRepository;
        this.descriptorService = descriptorService;
    }

    @Transactional
    public ConteudoIaResponse gerarConteudo(
            InternalRequestContext context,
            UUID planejamentoId,
            GerarConteudoIaRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String escolaNome = nomeEscola(context.escolaId());
        String conteudoGerado = montarConteudoGerado(request);

        InteracaoJpaEntity interaction = new InteracaoJpaEntity();
        interaction.setId(UUID.randomUUID());
        interaction.setEscolaId(context.escolaId());
        interaction.setEscolaNome(escolaNome);
        interaction.setPlanejamentoBimestralId(planejamentoId);
        interaction.setUsuarioId(context.usuarioId());
        interaction.setPromptProfessor(request.promptProfessor());
        interaction.setRespostaIa(conteudoGerado);
        interaction.setModeloIa("LOCAL_RULE_BASED");
        interaction.setCreatedAt(now);
        interactionRepository.save(interaction);

        ConteudoGeradoJpaEntity content = new ConteudoGeradoJpaEntity();
        content.setId(UUID.randomUUID());
        content.setEscolaId(context.escolaId());
        content.setEscolaNome(escolaNome);
        content.setPlanejamentoBimestralId(planejamentoId);
        content.setInteracao(interaction);
        content.setTitulo(tituloConteudo(request));
        content.setConteudo(conteudoGerado);
        content.setVersao(1);
        content.setHashConteudo(hashConteudo(conteudoGerado));
        content.setAprovadoPeloProfessor(false);
        content.setReutilizavel(!Boolean.FALSE.equals(request.reutilizavel()));
        content.setAtivo(true);
        content.setStatus("GERADO");
        content.setTipoConteudo(request.tipoConteudo());
        content.setCreatedAt(now);
        content.setUpdatedAt(now);
        contentRepository.save(content);

        return toResponse(content);
    }

    private ConteudoIaResponse toResponse(ConteudoGeradoJpaEntity entity) {
        return new ConteudoIaResponse(
                entity.getId(),
                entity.getPlanejamentoBimestralId(),
                entity.getInteracao() == null ? null : entity.getInteracao().getId(),
                entity.getEscolaId(),
                entity.getEscolaNome(),
                entity.getTitulo(),
                entity.getConteudo(),
                entity.getVersao(),
                entity.getHashConteudo(),
                entity.isAprovadoPeloProfessor(),
                entity.isReutilizavel(),
                entity.isAtivo(),
                entity.getStatus(),
                descriptorService.statusDescricao(entity.getStatus()),
                entity.getTipoConteudo(),
                descriptorService.tipoConteudoDescricao(entity.getTipoConteudo()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private String tituloConteudo(GerarConteudoIaRequest request) {
        if (request.titulo() != null && !request.titulo().isBlank()) {
            return request.titulo().trim();
        }
        return "Sugestao - " + request.tipoConteudo().trim();
    }

    private String montarConteudoGerado(GerarConteudoIaRequest request) {
        String titulo = tituloConteudo(request);
        return """
                Titulo: %s

                Tipo de conteudo: %s

                Orientacao do professor:
                %s
                """.formatted(titulo, request.tipoConteudo().trim(), request.promptProfessor().trim());
    }

    private String nomeEscola(UUID escolaId) {
        return "Escola " + escolaId.toString().substring(0, 8);
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

