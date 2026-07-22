package br.com.escola.planningaiservice.infra.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.planningaiservice.infra.config.PlanejamentoBackfillProperties;

@Component
@ConditionalOnBean(PlanejamentoBackfillExecutor.class)
@Order(1)
public class PlanejamentoBackfillRunner implements ApplicationRunner {
    private final PlanejamentoBackfillExecutor executor;
    private final PlanejamentoBackfillProperties properties;

    public PlanejamentoBackfillRunner(PlanejamentoBackfillExecutor executor, PlanejamentoBackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.failOnMismatch() && !executor.execute().reconciled()) {
            throw new IllegalStateException("planning-ai-backfill-not-reconciled");
        }
    }
}
