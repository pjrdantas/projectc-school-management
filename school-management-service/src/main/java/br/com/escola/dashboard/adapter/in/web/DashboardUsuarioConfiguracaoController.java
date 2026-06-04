package br.com.escola.dashboard.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardUsuarioConfiguracaoRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardUsuarioConfiguracaoResponse;
import br.com.escola.dashboard.application.service.DashboardUsuarioConfiguracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dashboard/usuarios")
@Tag(name = "Dashboard usuario")
public class DashboardUsuarioConfiguracaoController {

    private final DashboardUsuarioConfiguracaoService service;

    public DashboardUsuarioConfiguracaoController(DashboardUsuarioConfiguracaoService service) {
        this.service = service;
    }

    @GetMapping("/{usuarioId}/configuracoes")
    @Operation(summary = "Lista configurações de dashboard por usuário")
    public List<DashboardUsuarioConfiguracaoResponse> listar(
            @PathVariable @NonNull UUID usuarioId,
            @RequestParam(required = false) UUID dashboardId) {
        return service.listar(usuarioId, dashboardId);
    }

    @PutMapping("/{usuarioId}/widgets/{dashboardWidgetId}/configuracao")
    @Operation(summary = "Salva configuração de widget por usuário")
    public DashboardUsuarioConfiguracaoResponse salvar(
            @PathVariable @NonNull UUID usuarioId,
            @PathVariable @NonNull UUID dashboardWidgetId,
            @Valid @RequestBody DashboardUsuarioConfiguracaoRequest request) {
        return service.salvar(usuarioId, dashboardWidgetId, request);
    }

    @DeleteMapping("/{usuarioId}/widgets/{dashboardWidgetId}/configuracao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove configuração de widget por usuário")
    public void excluir(
            @PathVariable @NonNull UUID usuarioId,
            @PathVariable @NonNull UUID dashboardWidgetId) {
        service.excluir(usuarioId, dashboardWidgetId);
    }
}
