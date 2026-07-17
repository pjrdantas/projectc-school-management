package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.service.IdentityTenantCutoverDecision;

public interface IdentityTenantObservabilityPort {

    void recordDirectLegacy(IdentityTenantCutoverDecision decision);

    void recordServiceSuccess(IdentityTenantCutoverDecision decision, String target);

    void recordServiceFailure(IdentityTenantCutoverDecision decision, String target, Throwable error);

    void recordFallbackToLegacy(IdentityTenantCutoverDecision decision, String target, Throwable error);
}

