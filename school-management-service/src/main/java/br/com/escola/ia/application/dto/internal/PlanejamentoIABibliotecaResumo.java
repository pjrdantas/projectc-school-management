package br.com.escola.ia.application.dto.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlanejamentoIABibliotecaResumo(
        UUID id,
        UUID escolaId,
        String escolaNome,
        UUID professorId,
        String professorNome,
        UUID disciplinaId,
        String disciplinaNome,
        String tipoConteudo,
        String tipoConteudoDescricao,
        String titulo,
        String tema,
        String conteudo,
        String origem,
        Boolean reutilizavel,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
