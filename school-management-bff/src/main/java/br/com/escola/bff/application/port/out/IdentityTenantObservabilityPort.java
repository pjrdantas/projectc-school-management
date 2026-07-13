package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.service.IdentityTenantCutoverDecision;

public interface IdentityTenantObservabilityPort {

    void recordDirectMonolith(IdentityTenantCutoverDecision decision);

    void recordServiceSuccess(IdentityTenantCutoverDecision decision, String target);

    void recordServiceFailure(IdentityTenantCutoverDecision decision, String target, Throwable error);

    void recordFallbackToMonolith(IdentityTenantCutoverDecision decision, String target, Throwable error);
}
