package br.com.escola.professor.adapter.in.web.dto;

public record DiarioClasseAvaliacaoResponse(
        String idAvaliacao,
        String data,
        String descricao,
        String turma,
        String valor) {
}
