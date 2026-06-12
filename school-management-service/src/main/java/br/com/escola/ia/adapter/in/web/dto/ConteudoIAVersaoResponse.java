package br.com.escola.ia.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConteudoIAVersaoResponse(
        UUID id,
        UUID conteudoGeradoId,
        Integer numeroVersao,
        String conteudo,
        String motivoAlteracao,
        LocalDateTime createdAt) {
}
