package br.com.escola.bff.infra.observability;

import org.springframework.stereotype.Component;

import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.service.CatalogReadCutoverDecision;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MicrometerCatalogReadObservability implements CatalogReadObservabilityPort {

    private final MeterRegistry meterRegistry;

    public MicrometerCatalogReadObservability(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordCatalogSuccess(CatalogReadCutoverDecision decision) {
        routeCounter(decision, "catalog", "success").increment();
    }

    @Override
    public void recordCatalogFailure(CatalogReadCutoverDecision decision, Throwable error) {
        routeCounter(decision, "catalog", "failure").increment();
        errorCounter(decision, error).increment();
    }

    private Counter routeCounter(CatalogReadCutoverDecision decision, String target, String outcome) {
        return Counter.builder("bff.catalog.read.route.total")
                .description("Total de roteamentos read-only do catalogo por rota, alvo e resultado")
                .tag("route", routeTag(decision))
                .tag("target", target)
                .tag("reason", decision.reason())
                .tag("outcome", outcome)
                .register(meterRegistry);
    }

    private Counter errorCounter(CatalogReadCutoverDecision decision, Throwable error) {
        return Counter.builder("bff.catalog.read.catalog.error.total")
                .description("Falhas do academic-catalog-service durante cutover read-only")
                .tag("route", routeTag(decision))
                .tag("reason", decision.reason())
                .tag("exception", error.getClass().getSimpleName())
                .register(meterRegistry);
    }

    private String routeTag(CatalogReadCutoverDecision decision) {
        return decision.route().name().toLowerCase();
    }
}

