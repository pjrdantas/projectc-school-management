package br.com.escola.catalog.application.dto;

import java.util.UUID;

public record TurnoResponse(UUID id, String codigo, String descricao) {
}
