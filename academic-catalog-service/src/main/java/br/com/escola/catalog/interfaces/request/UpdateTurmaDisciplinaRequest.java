package br.com.escola.catalog.interfaces.request;

import jakarta.validation.constraints.Min;

public record UpdateTurmaDisciplinaRequest(@Min(1) Integer cargaHoraria) {
}
