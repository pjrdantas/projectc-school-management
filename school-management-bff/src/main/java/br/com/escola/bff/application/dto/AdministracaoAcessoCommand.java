package br.com.escola.bff.application.dto;

import java.util.UUID;

import br.com.escola.bff.application.model.OperacaoAcesso;
import br.com.escola.bff.application.model.RecursoAcesso;

public record AdministracaoAcessoCommand(
        RecursoAcesso recurso,
        OperacaoAcesso operacao,
        UUID recursoId,
        String requestBody,
        String authorization,
        String correlationId) {
}
