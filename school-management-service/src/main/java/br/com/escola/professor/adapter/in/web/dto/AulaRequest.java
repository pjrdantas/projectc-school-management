package br.com.escola.professor.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AulaRequest(
        @NotNull UUID professorTurmaDisciplinaId,
        @NotNull LocalDate dataAula,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        String conteudoMinistrado,
        @Size(max = 4000) String observacao,
        Boolean realizada) {
}
