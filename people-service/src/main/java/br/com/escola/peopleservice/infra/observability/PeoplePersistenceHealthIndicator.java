package br.com.escola.peopleservice.infra.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.service.PeopleDataAccessPolicy;
import br.com.escola.peopleservice.infra.config.PeopleRuntimeProperties;

@Component("peopleReadModel")
public class PeoplePersistenceHealthIndicator implements HealthIndicator {

    private final PeopleRuntimeProperties properties;
    private final PeopleDataAccessPolicy readRoutingPolicy;

    public PeoplePersistenceHealthIndicator(
            PeopleRuntimeProperties properties,
            PeopleDataAccessPolicy readRoutingPolicy) {
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
