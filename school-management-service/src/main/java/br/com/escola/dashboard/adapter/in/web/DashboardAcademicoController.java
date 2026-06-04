package br.com.escola.dashboard.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.application.service.DashboardAcademicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard/academico")
@Tag(name = "Dashboard academico")
public class DashboardAcademicoController {

    private final DashboardAcademicoService dashboardAcademicoService;

    public DashboardAcademicoController(DashboardAcademicoService dashboardAcademicoService) {
        this.dashboardAcademicoService = dashboardAcademicoService;
    }

    @GetMapping
    @Operation(summary = "Consulta dashboard academico-operacional")
    public DashboardAcademicoResponse consultar() {
        return dashboardAcademicoService.consultar();
    }
}
