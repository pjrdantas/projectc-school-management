package br.com.escola.professor.adapter.in.web.dto;

public record DiarioClasseConteudoRequest(
        String idPlanejamentoAula,
        String periodo,
        String descricao,
        Boolean alterado,
        String observacaoJustificativa) {
}
