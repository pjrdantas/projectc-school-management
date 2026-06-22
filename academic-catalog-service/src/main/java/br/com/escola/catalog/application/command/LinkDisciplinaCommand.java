package br.com.escola.catalog.application.command;

import java.util.UUID;

public record LinkDisciplinaCommand(UUID disciplinaId, Integer cargaHoraria) {
}
