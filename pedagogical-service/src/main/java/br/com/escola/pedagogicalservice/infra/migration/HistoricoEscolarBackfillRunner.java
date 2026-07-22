package br.com.escola.pedagogicalservice.infra.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.pedagogicalservice.infra.config.HistoricoEscolarBackfillProperties;
import br.com.escola.pedagogicalservice.infra.persistence.HistoricoEscolarBackfillExecutor;

@Component
@ConditionalOnBean(HistoricoEscolarBackfillExecutor.class)
@Order(1)
public class HistoricoEscolarBackfillRunner implements ApplicationRunner {

    private final HistoricoEscolarBackfillExecutor executor;
    private final HistoricoEscolarBackfillProperties properties;

    public HistoricoEscolarBackfillRunner(
            HistoricoEscolarBackfillExecutor executor, HistoricoEscolarBackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.failOnMismatch() && !executor.execute().reconciled()) {
            throw new IllegalStateException("pedagogical-history-backfill-not-reconciled");
        }
    }
}
