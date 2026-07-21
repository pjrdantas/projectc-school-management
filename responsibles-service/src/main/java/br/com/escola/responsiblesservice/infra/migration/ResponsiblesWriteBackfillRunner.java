package br.com.escola.responsiblesservice.infra.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.infra.config.ResponsiblesWriteBackfillProperties;
import br.com.escola.responsiblesservice.infra.persistence.ResponsiblesWriteBackfillExecutor;
import br.com.escola.responsiblesservice.infra.persistence.ResponsiblesWriteBackfillReport;

@Component
@ConditionalOnBean(ResponsiblesWriteBackfillExecutor.class)
@Order(1)
public class ResponsiblesWriteBackfillRunner implements ApplicationRunner {

    private final ResponsiblesWriteBackfillExecutor executor;
    private final ResponsiblesWriteBackfillProperties properties;

    public ResponsiblesWriteBackfillRunner(
            ResponsiblesWriteBackfillExecutor executor,
            ResponsiblesWriteBackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        ResponsiblesWriteBackfillReport report = executor.execute();
        if (properties.failOnMismatch() && !report.reconciled()) {
            throw new IllegalStateException("responsibles-write-backfill-not-reconciled");
        }
    }
}
