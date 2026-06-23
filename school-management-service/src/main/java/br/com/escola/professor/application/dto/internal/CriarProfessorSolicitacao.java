package br.com.escola.professor.application.dto.internal;

import java.util.UUID;

public record CriarProfessorSolicitacao(
        UUID funcionarioId,
        String registroProfissional,
        String formacao,
        Boolean ativo
) {
}
