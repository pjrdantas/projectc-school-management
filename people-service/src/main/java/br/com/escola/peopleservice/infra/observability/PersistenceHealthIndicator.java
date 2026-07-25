package br.com.escola.peopleservice.infra.observability;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.service.DataAccessPolicy;
import br.com.escola.peopleservice.infra.config.RuntimeProperties;

@Component("peopleReadModel")
public class PersistenceHealthIndicator implements HealthIndicator {

    private final RuntimeProperties properties;
    private final DataAccessPolicy readRoutingPolicy;

    public PersistenceHealthIndicator(
            RuntimeProperties properties,
            DataAccessPolicy readRoutingPolicy) {
        this.properties = properties;
        this.readRoutingPolicy = readRoutingPolicy;
    }

    @Override
    public Health health() {
        boolean autonomous = properties.enabled()
                && properties.localReadRoutingEnabled()
                && !properties.fallbackEnabled();
        Health.Builder builder = autonomous ? Health.up() : Health.outOfService();
        return builder
                .withDetail("operationalMode", autonomous ? "local_only" : "invalid_configuration")
                .withDetail("localReadRoutingEnabled", properties.localReadRoutingEnabled())
                .withDetail("fallbackEnabled", properties.fallbackEnabled())
                .withDetail("routes", readRoutingPolicy.avaliarTodas())
                .build();
    }
}
