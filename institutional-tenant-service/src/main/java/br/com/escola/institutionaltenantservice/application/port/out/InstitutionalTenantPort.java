package br.com.escola.institutionaltenantservice.application.port.out;

import java.util.List;

import br.com.escola.institutionaltenantservice.application.context.InternalRequestContext;
import br.com.escola.institutionaltenantservice.application.dto.TenantEscolaResponse;

public interface InstitutionalTenantPort {

    List<TenantEscolaResponse> listarEscolasDisponiveis(String authorization, InternalRequestContext context);
}
