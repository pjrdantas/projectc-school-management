package br.com.escola.compartilhado.pessoa.dto.internal;

import java.util.List;

public record PessoaConsultaCadastralPageInternalResponse(
        List<PessoaAlunoResponsaveisInternalResponse> content,
        long totalElements,
        int page,
        int size) {
}
