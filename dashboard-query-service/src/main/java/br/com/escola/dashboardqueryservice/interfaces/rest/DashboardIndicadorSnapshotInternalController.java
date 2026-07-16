package br.com.escola.dashboardqueryservice.interfaces.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.dashboardqueryservice.application.context.InternalHeaders;
import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardIndicadorSnapshotUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class DashboardIndicadorSnapshotInternalController {

    private final DashboardIndicadorSnapshotUseCase dashboardIndicadorSnapshotUseCase;

    public DashboardIndicadorSnapshotInternalController(DashboardIndicadorSnapshotUseCase dashboardIndicadorSnapshotUseCase) {
        this.dashboardIndicadorSnapshotUseCase = dashboardIndicadorSnapshotUseCase;
    }

    @GetMapping("/dashboard/snapshots/publicos/{publicoCodigo}")
    public List<DashboardIndicadorSnapshotResponse> listarPorPublicoCodigo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull String publicoCodigo,
            @RequestParam(required = false) LocalDate referenciaData) {
        return dashboardIndicadorSnapshotUseCase.listarPorPublicoCodigo(authorization, context, publicoCodigo, referenciaData);
    }
}
