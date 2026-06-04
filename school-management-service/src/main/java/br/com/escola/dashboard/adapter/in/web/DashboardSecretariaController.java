package br.com.escola.dashboard.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;
import br.com.escola.dashboard.application.service.DashboardSecretariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard/secretaria")
@Tag(name = "Dashboard secretaria")
public class DashboardSecretariaController {

    private final DashboardSecretariaService dashboardSecretariaService;

    public DashboardSecretariaController(DashboardSecretariaService dashboardSecretariaService) {
        this.dashboardSecretariaService = dashboardSecretariaService;
    }

    @GetMapping
    @Operation(summary = "Consulta dashboard da secretaria")
    public DashboardSecretariaResponse consultar() {
        return dashboardSecretariaService.consultar();
    }
}
