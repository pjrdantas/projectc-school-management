package br.com.escola.bff.infra.observability;

import org.springframework.stereotype.Component;

import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.service.IdentityTenantCutoverDecision;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MicrometerIdentityTenantObservability implements IdentityTenantObservabilityPort {

    private final MeterRegistry meterRegistry;

    public MicrometerIdentityTenantObservability(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordServiceSuccess(IdentityTenantCutoverDecision decision, String target) {
        routeCounter(decision, target, "success").increment();
    }

    @Override
    public void recordServiceFailure(IdentityTenantCutoverDecision decision, String target, Throwable error) {
        routeCounter(decision, target, "failure").increment();
        errorCounter(decision, target, error).increment();
    }

    private Counter routeCounter(IdentityTenantCutoverDecision decision, String target, String outcome) {
        return Counter.builder("bff.identity_tenant.route.total")
                .description("Total de roteamentos do bloco identity/tenant por rota, alvo e resultado")
                .tag("route", decision.route().name().toLowerCase())
                .tag("target", target)
                .tag("reason", decision.reason())
                .tag("outcome", outcome)
                .register(meterRegistry);
    }

    private Counter errorCounter(IdentityTenantCutoverDecision decision, String target, Throwable error) {
        return Counter.builder("bff.identity_tenant.error.total")
                .description("Falhas dos servicos novos do bloco identity/tenant")
                .tag("route", decision.route().name().toLowerCase())
                .tag("target", target)
                .tag("reason", decision.reason())
                .tag("exception", error.getClass().getSimpleName())
                .register(meterRegistry);
    }

}

