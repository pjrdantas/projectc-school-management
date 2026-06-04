package br.com.escola.aluno.adapter.in.web;

import java.util.List;

import br.com.escola.responsavel.adapter.in.web.vinculo.ResponsavelVinculadoResponse;

public record AlunoFichaResponse(
        AlunoResponse aluno,
        List<ResponsavelVinculadoResponse> responsaveis) {
}
