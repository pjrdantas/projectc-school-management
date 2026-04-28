package br.com.escola.responsavelmanagement.application.dto.consulta;

import java.util.List;

public record ConsultaCadastralPageOutput(
        List<AlunoComResponsaveisOutput> content,
        long totalElements,
        int page,
        int size) {
}
