package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.UUID;

public record DashboardPublicoResponse(
        UUID id,
        String codigo,
        String descricao) {
}
