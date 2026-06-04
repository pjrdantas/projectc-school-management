package br.com.escola.dashboard.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardAlertaResponse;
import br.com.escola.dashboard.application.service.DashboardAlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard/alertas")
@Tag(name = "Dashboard alertas")
public class DashboardAlertaController {

    private final DashboardAlertaService dashboardAlertaService;

    public DashboardAlertaController(DashboardAlertaService dashboardAlertaService) {
        this.dashboardAlertaService = dashboardAlertaService;
    }

    @GetMapping
    @Operation(summary = "Consulta alertas críticos do dashboard")
    public List<DashboardAlertaResponse> consultar(
            @RequestParam @NonNull String publicoCodigo,
            @RequestParam(required = false) UUID professorId) {
        return dashboardAlertaService.consultar(publicoCodigo, professorId);
    }
}
