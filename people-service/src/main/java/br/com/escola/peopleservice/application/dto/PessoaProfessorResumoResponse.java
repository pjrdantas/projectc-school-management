package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaProfessorResumoResponse(
        UUID professorId,
        UUID pessoaId,
        UUID funcionarioId,
        UUID escolaId,
        String nomeCompleto,
        boolean ativo) {
}


