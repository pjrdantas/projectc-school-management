package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaAlunoVinculoResponse(
        UUID alunoId,
        UUID pessoaId,
        UUID escolaId) {
}
