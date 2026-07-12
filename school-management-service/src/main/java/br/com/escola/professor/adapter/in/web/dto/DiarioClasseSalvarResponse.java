package br.com.escola.professor.adapter.in.web.dto;

import java.time.LocalDateTime;

public record DiarioClasseSalvarResponse(
        String idDiarioClasse,
        String status,
        String mensagem,
        LocalDateTime salvoEm,
        Boolean bloqueado) {
}
