package br.com.escola.compartilhado.pessoa.dto.internal;

import java.util.List;

public record PessoaConsultaCadastralPage(
        List<PessoaAlunoResponsaveisResumo> content,
        long totalElements,
        int page,
        int size) {
}
