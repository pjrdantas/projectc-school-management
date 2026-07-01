package br.com.escola.professor.adapter.in.web.dto;

import java.util.List;

public record DiarioClasseResponse(
        DiarioClasseCabecalhoResponse cabecalho,
        List<DiarioClasseAlunoResponse> alunos,
        List<DiarioClasseConteudoPlanejadoResponse> conteudosPlanejados,
        List<String> observacoes,
        List<DiarioClasseAvaliacaoResponse> avaliacoes,
        DiarioClasseAssinaturaResponse assinatura,
        Boolean bloqueado) {
}
