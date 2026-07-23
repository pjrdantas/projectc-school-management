package br.com.escola.planningaiservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlanejamentoBimestralResponse(
        UUID id,
        UUID professorTurmaDisciplinaId,
        UUID escolaId,
        UUID periodoAvaliativoId,
        String status,
        String titulo,
        String temaPrincipal,
        String descricaoInicial,
        String objetivoGeral,
        String observacaoProfessor,
        String conteudoFinalAprovado,
        boolean reutilizavel,
        boolean criadoComAuxilioIa,
        boolean aprovadoPeloProfessor,
        LocalDateTime dataAprovacao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
