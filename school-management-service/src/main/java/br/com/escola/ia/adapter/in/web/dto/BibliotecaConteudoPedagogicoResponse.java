package br.com.escola.ia.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BibliotecaConteudoPedagogicoResponse(
        UUID id,
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
