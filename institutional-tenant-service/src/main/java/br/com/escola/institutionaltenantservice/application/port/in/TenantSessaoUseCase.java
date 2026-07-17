package br.com.escola.institutionaltenantservice.application.port.in;

import java.util.List;

import br.com.escola.institutionaltenantservice.application.context.InternalRequestContext;
import br.com.escola.institutionaltenantservice.application.dto.TenantAtivoResponse;
import br.com.escola.institutionaltenantservice.application.dto.TenantEscolaResponse;

public interface TenantSessaoUseCase {

    List<TenantEscolaResponse> listarEscolasDisponiveis(String authorization, InternalRequestContext context);

    TenantAtivoResponse consultarTenantAtivo(String authorization, InternalRequestContext context);
}

