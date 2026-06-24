package br.com.escola.professorservice.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProfessorAlocacaoResponse(
        UUID id,
        UUID professorId,
        String professorNome,
        UUID turmaDisciplinaId,
        UUID turmaId,
        String turmaNome,
        UUID disciplinaId,
        String disciplinaNome,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativo,
        LocalDateTime createdAt) {
}
