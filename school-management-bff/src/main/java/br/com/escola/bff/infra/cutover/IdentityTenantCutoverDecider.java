package br.com.escola.bff.infra.cutover;

import org.springframework.stereotype.Component;

import br.com.escola.bff.application.port.out.IdentityTenantCutoverPolicyPort;
import br.com.escola.bff.application.service.IdentityTenantCutoverDecision;
import br.com.escola.bff.application.service.IdentityTenantRoute;
import br.com.escola.bff.infra.config.IdentityTenantCutoverProperties;

@Component
public class IdentityTenantCutoverDecider implements IdentityTenantCutoverPolicyPort {

    private final IdentityTenantCutoverProperties properties;

    public IdentityTenantCutoverDecider(IdentityTenantCutoverProperties properties) {
        this.properties = properties;
    }

    @Override
    public IdentityTenantCutoverDecision decision(IdentityTenantRoute route) {
        if (!properties.enabled()) {
            return new IdentityTenantCutoverDecision(route, false, "cutover_disabled");
        }
        if (!properties.routeEnabled(route)) {
            return new IdentityTenantCutoverDecision(route, false, "route_disabled");
        }
        return new IdentityTenantCutoverDecision(route, true, "service_enabled");
    }

    @Override
    public boolean fallbackToMonolithOnError() {
        return properties.fallbackToMonolithOnError();
    }
}
