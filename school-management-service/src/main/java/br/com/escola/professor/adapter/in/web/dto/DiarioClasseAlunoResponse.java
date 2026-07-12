package br.com.escola.professor.adapter.in.web.dto;

import java.util.Map;

public record DiarioClasseAlunoResponse(
        String idAluno,
        Integer numeroChamada,
        String nome,
        Map<String, String> frequencias) {
}
