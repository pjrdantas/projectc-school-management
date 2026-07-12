package br.com.escola.compartilhado.pessoa.dto.internal;

import java.util.List;
import java.util.UUID;

public record PessoaEnderecoWriteInternalResponse(
        String commandId,
        UUID pessoaId,
        UUID enderecoId,
        UUID pessoaEnderecoId,
        String status,
        String selectedSource,
        boolean persistedLocally,
        boolean fallbackRequired,
        List<String> warnings) {
}
