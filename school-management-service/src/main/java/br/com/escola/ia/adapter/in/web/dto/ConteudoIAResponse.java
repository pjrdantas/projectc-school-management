package br.com.escola.ia.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConteudoIAResponse(
        UUID id,
        UUID planejamentoBimestralId,
        UUID interacaoId,
        UUID escolaId,
        String escolaNome,
        String titulo,
        String conteudo,
        Integer versao,
        String hashConteudo,
        Boolean aprovadoPeloProfessor,
        Boolean reutilizavel,
        Boolean ativo,
        String status,
        String statusDescricao,
        String tipoConteudo,
        String tipoConteudoDescricao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
