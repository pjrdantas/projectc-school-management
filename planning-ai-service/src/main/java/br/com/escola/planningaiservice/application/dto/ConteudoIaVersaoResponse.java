package br.com.escola.planningaiservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConteudoIaVersaoResponse(
        UUID id,
        UUID conteudoGeradoId,
        Integer numeroVersao,
        String conteudo,
        String motivoAlteracao,
        LocalDateTime createdAt) {
}
