package br.com.escola.dashboard.adapter.in.web;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorResponse;
import br.com.escola.dashboard.application.service.DashboardProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard/professores")
@Tag(name = "Dashboard professor")
public class DashboardProfessorController {

    private final DashboardProfessorService dashboardProfessorService;

    public DashboardProfessorController(DashboardProfessorService dashboardProfessorService) {
        this.dashboardProfessorService = dashboardProfessorService;
    }

    @GetMapping("/{professorId}")
    @Operation(summary = "Consulta dashboard do professor")
    public DashboardProfessorResponse consultar(@PathVariable @NonNull UUID professorId) {
        return dashboardProfessorService.consultar(professorId);
    }
}
