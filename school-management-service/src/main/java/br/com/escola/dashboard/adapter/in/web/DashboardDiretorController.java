package br.com.escola.dashboard.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardDiretorResponse;
import br.com.escola.dashboard.application.service.DashboardDiretorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard/diretor")
@Tag(name = "Dashboard diretor")
public class DashboardDiretorController {

    private final DashboardDiretorService dashboardDiretorService;

    public DashboardDiretorController(DashboardDiretorService dashboardDiretorService) {
        this.dashboardDiretorService = dashboardDiretorService;
    }

    @GetMapping
    @Operation(summary = "Consulta dashboard do diretor")
    public DashboardDiretorResponse consultar() {
        return dashboardDiretorService.consultar();
    }
}
