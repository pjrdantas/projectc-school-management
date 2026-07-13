package br.com.escola.matricula.adapter.in.web.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatriculaEtapaInternalResponse(
        UUID id,
        String descricao,
        Integer ordem,
        String status,
        LocalDateTime dataInicio,
        LocalDateTime dataConclusao,
        String observacao) {
}
