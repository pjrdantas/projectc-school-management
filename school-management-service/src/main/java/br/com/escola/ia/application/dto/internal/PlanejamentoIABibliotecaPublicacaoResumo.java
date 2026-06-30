package br.com.escola.ia.application.dto.internal;

import java.util.UUID;

public record PlanejamentoIABibliotecaPublicacaoResumo(
        UUID professorId,
        UUID disciplinaId,
        UUID tipoConteudoId,
        String titulo,
        String tema,
        String conteudo,
        String origem,
        Boolean reutilizavel) {
}
