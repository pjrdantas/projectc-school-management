package br.com.escola.bff.application.dto;

import java.util.UUID;

public record TurmaDisciplinaLinkCommand(
        UUID turmaId,
        UUID disciplinaId,
        Integer cargaHoraria
) {}
