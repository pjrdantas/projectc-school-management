package br.com.escola.planningaiservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlanejamentoBimestralRequest(
        @NotNull UUID professorTurmaDisciplinaId,
        UUID periodoAvaliativoId,
        @NotBlank @Size(max = 180) String titulo,
        @NotBlank @Size(max = 180) String temaPrincipal,
        @NotBlank String descricaoInicial,
        String objetivoGeral,
        String observacaoProfessor,
        String conteudoFinalAprovado,
        Boolean reutilizavel,
        Boolean criadoComAuxilioIa) {
}
