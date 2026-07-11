package br.com.escola.peopleservice.application.dto;

import java.util.List;
import java.util.UUID;

public record PessoaEnderecoWriteResult(
        UUID commandId,
        UUID pessoaId,
        UUID enderecoId,
        UUID pessoaEnderecoId,
        String status,
        String selectedSource,
        boolean persistedLocally,
        boolean fallbackRequired,
        List<String> warnings) {
}


