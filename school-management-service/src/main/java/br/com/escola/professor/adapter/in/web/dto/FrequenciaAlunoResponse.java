package br.com.escola.professor.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FrequenciaAlunoResponse(
        UUID id,
        UUID aulaId,
        UUID matriculaId,
        UUID alunoId,
        String alunoNome,
        String situacao,
        String justificativa,
        LocalDateTime createdAt) {
}
