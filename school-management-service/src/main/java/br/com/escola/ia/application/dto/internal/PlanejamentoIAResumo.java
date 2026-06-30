package br.com.escola.ia.application.dto.internal;

import java.util.UUID;

public record PlanejamentoIAResumo(
        UUID planejamentoId,
        String titulo,
        String temaPrincipal,
        String descricaoInicial,
        String objetivoGeral,
        String turmaNome,
        String disciplinaNome) {
}
