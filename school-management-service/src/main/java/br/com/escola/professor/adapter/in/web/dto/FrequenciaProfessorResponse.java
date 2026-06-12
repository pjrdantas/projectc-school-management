package br.com.escola.professor.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FrequenciaProfessorResponse(
        UUID id,
        UUID aulaId,
        UUID professorId,
        String professorNome,
        UUID escolaId,
        String escolaNome,
        Boolean presente,
        String justificativa,
        LocalDateTime createdAt) {
}
