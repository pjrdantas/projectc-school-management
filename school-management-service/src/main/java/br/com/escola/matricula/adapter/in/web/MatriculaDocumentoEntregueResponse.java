package br.com.escola.matricula.adapter.in.web;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatriculaDocumentoEntregueResponse(
        UUID id,
        UUID matriculaId,
        UUID documentoId,
        UUID tipoDocumentoId,
        String nomeArquivo,
        String urlArquivo,
        Boolean conferido,
        UUID conferidoPor,
        LocalDateTime dataConferencia,
        String observacao,
        LocalDateTime createdAt) {
}
