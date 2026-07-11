package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaResponsavelVinculoResponse(
        UUID responsavelId,
        UUID pessoaId,
        UUID escolaId) {
}
