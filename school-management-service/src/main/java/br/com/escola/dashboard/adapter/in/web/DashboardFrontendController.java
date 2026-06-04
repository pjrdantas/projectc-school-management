package br.com.escola.dashboard.adapter.in.web;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendResponse;
import br.com.escola.dashboard.application.service.DashboardFrontendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard/frontend")
@Tag(name = "Dashboard frontend")
public class DashboardFrontendController {

    private final DashboardFrontendService dashboardFrontendService;

    public DashboardFrontendController(DashboardFrontendService dashboardFrontendService) {
        this.dashboardFrontendService = dashboardFrontendService;
    }

    @GetMapping
    @Operation(summary = "Consulta pacote agregado para montagem do dashboard no frontend")
    public DashboardFrontendResponse consultar(
            @RequestParam @NonNull String publicoCodigo,
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) UUID professorId) {
        return dashboardFrontendService.consultar(publicoCodigo, usuarioId, professorId);
    }
}
