package br.com.escola.institutionaltenantservice.interfaces.rest;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.institutionaltenantservice.application.context.InternalHeaders;
import br.com.escola.institutionaltenantservice.application.context.InternalRequestContext;
import br.com.escola.institutionaltenantservice.application.dto.TenantAtivoResponse;
import br.com.escola.institutionaltenantservice.application.dto.TenantEscolaResponse;
import br.com.escola.institutionaltenantservice.application.port.in.TenantSessaoUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class TenantSessaoInternaController {

    private final TenantSessaoUseCase institutionalTenantUseCase;

    public TenantSessaoInternaController(TenantSessaoUseCase institutionalTenantUseCase) {
        this.institutionalTenantUseCase = institutionalTenantUseCase;
    }

    @GetMapping("/tenant/escolas")
    public List<TenantEscolaResponse> listarEscolasDisponiveis(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return institutionalTenantUseCase.listarEscolasDisponiveis(authorization, context);
    }

    @GetMapping("/tenant/ativa")
    public TenantAtivoResponse consultarTenantAtivo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return institutionalTenantUseCase.consultarTenantAtivo(authorization, context);
    }
}

