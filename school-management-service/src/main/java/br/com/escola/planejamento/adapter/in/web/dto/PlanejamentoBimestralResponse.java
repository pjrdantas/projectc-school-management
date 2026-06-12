package br.com.escola.planejamento.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PlanejamentoBimestralResponse(
        UUID id,
        UUID professorTurmaDisciplinaId,
        UUID professorId,
        String professorNome,
        UUID turmaId,
        String turmaNome,
        UUID disciplinaId,
        String disciplinaNome,
        UUID escolaId,
        String escolaNome,
        UUID periodoAvaliativoId,
        String periodoAvaliativoNome,
        String status,
        String statusDescricao,
        String titulo,
        String temaPrincipal,
        String descricaoInicial,
        String objetivoGeral,
        String observacaoProfessor,
        String conteudoFinalAprovado,
        Boolean reutilizavel,
        Boolean criadoComAuxilioIA,
        Boolean aprovadoPeloProfessor,
        LocalDateTime dataAprovacao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PlanejamentoBimestralAulaResponse> aulasPrevistas,
        List<PlanejamentoBimestralAvaliacaoResponse> avaliacoesPrevistas) {
}
