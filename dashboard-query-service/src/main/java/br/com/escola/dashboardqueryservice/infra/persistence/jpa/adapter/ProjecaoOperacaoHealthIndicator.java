package br.com.escola.dashboardqueryservice.infra.persistence.jpa.adapter;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("dashboardProjectionOperation")
public class ProjecaoOperacaoHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetail("operationalMode", "owner_driven_projections")
                .withDetail("scheduledSnapshotGeneration", false)
                .build();
    }
}
