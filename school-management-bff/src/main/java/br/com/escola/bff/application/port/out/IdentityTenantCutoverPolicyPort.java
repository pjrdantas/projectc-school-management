package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.service.IdentityTenantCutoverDecision;
import br.com.escola.bff.application.service.IdentityTenantRoute;

public interface IdentityTenantCutoverPolicyPort {

    IdentityTenantCutoverDecision decision(IdentityTenantRoute route);

    boolean fallbackToLegacyOnError();
}

