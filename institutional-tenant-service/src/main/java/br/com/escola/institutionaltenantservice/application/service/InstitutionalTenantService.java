package br.com.escola.institutionaltenantservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.institutionaltenantservice.application.context.InternalRequestContext;
import br.com.escola.institutionaltenantservice.application.dto.TenantAtivoResponse;
import br.com.escola.institutionaltenantservice.application.dto.TenantEscolaResponse;
import br.com.escola.institutionaltenantservice.application.exception.InstitutionalTenantServiceResourceNotFoundException;
import br.com.escola.institutionaltenantservice.application.port.in.InstitutionalTenantUseCase;
import br.com.escola.institutionaltenantservice.application.port.out.InstitutionalTenantPort;

@Service
public class InstitutionalTenantService implements InstitutionalTenantUseCase {

    private final InstitutionalTenantPort institutionalTenantPort;

    public InstitutionalTenantService(InstitutionalTenantPort institutionalTenantPort) {
        this.institutionalTenantPort = institutionalTenantPort;
    }

    @Override
    public List<TenantEscolaResponse> listarEscolasDisponiveis(String authorization, InternalRequestContext context) {
        return institutionalTenantPort.listarEscolasDisponiveis(authorization, context);
    }

    @Override
    public TenantAtivoResponse consultarTenantAtivo(String authorization, InternalRequestContext context) {
        return institutionalTenantPort.listarEscolasDisponiveis(authorization, context).stream()
                .filter(TenantEscolaResponse::ativa)
                .findFirst()
                .map(escola -> new TenantAtivoResponse(escola.escolaId(), escola.escolaNome()))
                .orElseThrow(() -> new InstitutionalTenantServiceResourceNotFoundException(
                        "Tenant ativo nao encontrado para a sessao informada"));
    }
}
