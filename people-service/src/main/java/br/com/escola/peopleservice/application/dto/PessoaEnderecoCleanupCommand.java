package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaEnderecoCleanupCommand(
        UUID commandId,
        UUID pessoaId,
        UUID escolaId,
        boolean removeOrphanAddresses,
        String idempotencyKey,
        String requestedBy) {
}


