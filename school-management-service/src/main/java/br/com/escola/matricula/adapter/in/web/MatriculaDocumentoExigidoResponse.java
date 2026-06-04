package br.com.escola.matricula.adapter.in.web;

import java.util.UUID;

public record MatriculaDocumentoExigidoResponse(
        UUID id,
        UUID tipoMatriculaId,
        String tipoMatricula,
        UUID tipoDocumentoId,
        String tipoDocumento,
        String descricao,
        Boolean obrigatorio,
        Integer ordem,
        Boolean entregue,
        UUID documentoEntregueId,
        UUID documentoId) {
}
