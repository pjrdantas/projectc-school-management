package br.com.escola.matricula.application.dto.internal;

import java.util.UUID;

public record RegistrarMatriculaDocumentoEntregueSolicitacao(
        UUID documentoId,
        Boolean conferido,
        UUID conferidoPor,
        String observacao) {
}
