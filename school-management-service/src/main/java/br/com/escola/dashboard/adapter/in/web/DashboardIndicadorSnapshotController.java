package br.com.escola.dashboard.adapter.in.web;

import java.time.LocalDate;
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

import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorSnapshotRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorSnapshotResponse;
import br.com.escola.dashboard.application.service.DashboardIndicadorSnapshotService;
import br.com.escola.dashboard.application.service.DashboardSnapshotGeradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dashboard/snapshots")
@Tag(name = "Dashboard snapshots")
public class DashboardIndicadorSnapshotController {

    private final DashboardIndicadorSnapshotService service;
    private final DashboardSnapshotGeradorService geradorService;

    public DashboardIndicadorSnapshotController(
            DashboardIndicadorSnapshotService service,
            DashboardSnapshotGeradorService geradorService) {
        this.service = service;
        this.geradorService = geradorService;
    }

    @GetMapping
    @Operation(summary = "Lista snapshots de indicadores por público")
    public List<DashboardIndicadorSnapshotResponse> listar(
            @RequestParam @NonNull UUID publicoDashboardId,
            @RequestParam(required = false) LocalDate referenciaData) {
        return service.listar(publicoDashboardId, referenciaData);
    }

    @GetMapping("/publicos/{publicoCodigo}")
    @Operation(summary = "Lista snapshots de indicadores por código do público")
    public List<DashboardIndicadorSnapshotResponse> listarPorPublicoCodigo(
            @PathVariable @NonNull String publicoCodigo,
            @RequestParam(required = false) LocalDate referenciaData) {
        return service.listarPorPublicoCodigo(publicoCodigo, referenciaData);
    }

    @PutMapping
    @Operation(summary = "Salva snapshot de indicador")
    public DashboardIndicadorSnapshotResponse salvar(@Valid @RequestBody DashboardIndicadorSnapshotRequest request) {
        return service.salvar(request);
    }

    @PostMapping("/geracoes/professores/{professorId}")
    @Operation(summary = "Gera snapshots de indicadores do dashboard de um professor")
    public List<DashboardIndicadorSnapshotResponse> gerarProfessor(
            @PathVariable @NonNull UUID professorId,
            @RequestParam(required = false) LocalDate referenciaData) {
        return geradorService.gerarProfessor(professorId, referenciaData);
    }

    @PostMapping("/geracoes/{publicoCodigo}")
    @Operation(summary = "Gera snapshots de indicadores por público")
    public List<DashboardIndicadorSnapshotResponse> gerar(
            @PathVariable @NonNull String publicoCodigo,
            @RequestParam(required = false) LocalDate referenciaData) {
        return geradorService.gerar(publicoCodigo, referenciaData);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove snapshot de indicador")
    public void excluir(@PathVariable @NonNull UUID id) {
        service.excluir(id);
    }
}
