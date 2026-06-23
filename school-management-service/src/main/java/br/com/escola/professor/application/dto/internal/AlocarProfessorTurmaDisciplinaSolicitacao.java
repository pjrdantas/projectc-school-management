package br.com.escola.professor.application.dto.internal;

import java.time.LocalDate;
import java.util.UUID;

public record AlocarProfessorTurmaDisciplinaSolicitacao(
        UUID turmaDisciplinaId,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativo
) {
}
