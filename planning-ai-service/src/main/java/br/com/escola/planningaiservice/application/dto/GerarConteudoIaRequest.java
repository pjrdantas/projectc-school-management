package br.com.escola.planningaiservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GerarConteudoIaRequest(
        @NotBlank @Size(max = 4000) String promptProfessor,
        @NotBlank @Size(max = 60) String tipoConteudo,
        @Size(max = 180) String titulo,
        Boolean reutilizavel) {
}
