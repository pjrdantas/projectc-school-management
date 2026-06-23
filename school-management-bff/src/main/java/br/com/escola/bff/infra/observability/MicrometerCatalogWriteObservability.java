package br.com.escola.bff.infra.observability;

import org.springframework.stereotype.Component;

import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.service.CatalogWriteCutoverDecision;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MicrometerCatalogWriteObservability implements CatalogWriteObservabilityPort {

    private final MeterRegistry meterRegistry;

    public MicrometerCatalogWriteObservability(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordDirectMonolith(CatalogWriteCutoverDecision decision) {
        routeCounter(decision, "monolith", "success").increment();
    }

    @Override
    public void recordCatalogSuccess(CatalogWriteCutoverDecision decision) {
        routeCounter(decision, "catalog", "success").increment();
    }

    @Override
    public void recordCatalogFailure(CatalogWriteCutoverDecision decision, Throwable error) {
        routeCounter(decision, "catalog", "failure").increment();
        errorCounter(decision, error).increment();
    }

    private Counter routeCounter(CatalogWriteCutoverDecision decision, String target, String outcome) {
        return Counter.builder("bff_catalog_write_route_total")
                .tag("route", routeTag(decision))
                .tag("target", target)
                .tag("outcome", outcome)
                .tag("reason", decision.reason())
                .register(meterRegistry);
    }

    private Counter errorCounter(CatalogWriteCutoverDecision decision, Throwable error) {
        return Counter.builder("bff_catalog_write_error_total")
                .tag("route", routeTag(decision))
                .tag("reason", decision.reason())
                .tag("exception", error.getClass().getSimpleName())
                .register(meterRegistry);
    }

    private String routeTag(CatalogWriteCutoverDecision decision) {
        return switch (decision.route()) {
            case PERIODOS_LETIVOS -> "periodos_letivos";
            case DISCIPLINAS -> "disciplinas";
            case SERIES -> "series";
            case TURMAS -> "turmas";
        };
    }
}
