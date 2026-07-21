package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record AlunoFichaResponse(
        AlunoResponse aluno,
        List<PessoaResponsavelVinculadoResponse> responsaveis) {
}
