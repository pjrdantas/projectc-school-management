package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PessoaConsultaCadastralPageResponse(
        List<PessoaAlunoResponsaveisResponse> content,
        long totalElements,
        int page,
        int size) {
}
