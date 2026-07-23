package br.com.escola.pedagogicalservice.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AulaResponse(
        UUID id,
        UUID professorTurmaDisciplinaId,
        UUID professorId,
        String professorNome,
        UUID turmaId,
        String turmaNome,
        UUID escolaId,
        String escolaNome,
        UUID disciplinaId,
        String disciplinaNome,
        LocalDate dataAula,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        String conteudoMinistrado,
        String observacao,
        Boolean realizada,
        LocalDateTime createdAt) {
}
