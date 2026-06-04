package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.UUID;

public record DashboardAlertaResponse(
        String publicoCodigo,
        UUID professorId,
        String codigo,
        String severidade,
        String titulo,
        String mensagem,
        long valor,
        long limite) {
}
