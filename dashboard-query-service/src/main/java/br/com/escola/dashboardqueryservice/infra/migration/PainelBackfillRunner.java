package br.com.escola.dashboardqueryservice.infra.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.dashboardqueryservice.infra.config.PainelBackfillProperties;
import br.com.escola.dashboardqueryservice.infra.persistence.PainelBackfillExecutor;

@Component
@ConditionalOnBean(PainelBackfillExecutor.class)
@Order(1)
public class PainelBackfillRunner implements ApplicationRunner {

    private final PainelBackfillExecutor executor;
    private final PainelBackfillProperties properties;

    public PainelBackfillRunner(PainelBackfillExecutor executor, PainelBackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.failOnMismatch() && !executor.execute().reconciled()) {
            throw new IllegalStateException("dashboard-query-backfill-not-reconciled");
        }
    }
}
