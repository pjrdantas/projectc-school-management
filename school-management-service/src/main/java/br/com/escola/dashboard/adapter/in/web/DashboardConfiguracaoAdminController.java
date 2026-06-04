package br.com.escola.dashboard.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardConfiguracaoRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardPublicoRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardPublicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardWidgetRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardWidgetResponse;
import br.com.escola.dashboard.application.service.DashboardConfiguracaoAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dashboard/configuracoes")
@Tag(name = "Dashboard configuracao")
public class DashboardConfiguracaoAdminController {

    private final DashboardConfiguracaoAdminService service;

    public DashboardConfiguracaoAdminController(DashboardConfiguracaoAdminService service) {
        this.service = service;
    }

    @GetMapping("/publicos")
    @Operation(summary = "Lista públicos de dashboard")
    public List<DashboardPublicoResponse> listarPublicos() {
        return service.listarPublicos();
    }

    @PostMapping("/publicos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria público de dashboard")
    public DashboardPublicoResponse criarPublico(@Valid @RequestBody DashboardPublicoRequest request) {
        return service.criarPublico(request);
    }

    @PutMapping("/publicos/{id}")
    @Operation(summary = "Atualiza público de dashboard")
    public DashboardPublicoResponse atualizarPublico(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody DashboardPublicoRequest request) {
        return service.atualizarPublico(id, request);
    }

    @DeleteMapping("/publicos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove público de dashboard")
    public void excluirPublico(@PathVariable @NonNull UUID id) {
        service.excluirPublico(id);
    }

    @GetMapping("/dashboards")
    @Operation(summary = "Lista dashboards configurados")
    public List<DashboardConfiguracaoResponse> listarDashboards(
            @RequestParam(required = false) UUID publicoDashboardId,
            @RequestParam(required = false) String publicoCodigo) {
        if (publicoCodigo != null && !publicoCodigo.isBlank()) {
            return service.listarDashboardsPorPublicoCodigo(publicoCodigo);
        }
        return service.listarDashboards(publicoDashboardId);
    }

    @PostMapping("/dashboards")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria dashboard por público")
    public DashboardConfiguracaoResponse criarDashboard(@Valid @RequestBody DashboardConfiguracaoRequest request) {
        return service.criarDashboard(request);
    }

    @PutMapping("/dashboards/{id}")
    @Operation(summary = "Atualiza dashboard por público")
    public DashboardConfiguracaoResponse atualizarDashboard(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody DashboardConfiguracaoRequest request) {
        return service.atualizarDashboard(id, request);
    }

    @DeleteMapping("/dashboards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove dashboard")
    public void excluirDashboard(@PathVariable @NonNull UUID id) {
        service.excluirDashboard(id);
    }

    @GetMapping("/dashboards/{dashboardId}/widgets")
    @Operation(summary = "Lista widgets de um dashboard")
    public List<DashboardWidgetResponse> listarWidgets(@PathVariable @NonNull UUID dashboardId) {
        return service.listarWidgets(dashboardId);
    }

    @PostMapping("/widgets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria widget de dashboard")
    public DashboardWidgetResponse criarWidget(@Valid @RequestBody DashboardWidgetRequest request) {
        return service.criarWidget(request);
    }

    @PutMapping("/widgets/{id}")
    @Operation(summary = "Atualiza widget de dashboard")
    public DashboardWidgetResponse atualizarWidget(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody DashboardWidgetRequest request) {
        return service.atualizarWidget(id, request);
    }

    @DeleteMapping("/widgets/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove widget de dashboard")
    public void excluirWidget(@PathVariable @NonNull UUID id) {
        service.excluirWidget(id);
    }
}
