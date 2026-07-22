package br.com.escola.planningaiservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlanejamentoBimestralAulaResponse(
        UUID id,
        UUID planejamentoBimestralId,
        int numeroAula,
        String temaAula,
        LocalDateTime createdAt) {
}
