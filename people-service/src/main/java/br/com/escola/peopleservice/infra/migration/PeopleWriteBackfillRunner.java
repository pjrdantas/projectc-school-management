package br.com.escola.peopleservice.infra.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.infra.config.PeopleWriteBackfillProperties;
import br.com.escola.peopleservice.infra.persistence.PeopleWriteBackfillExecutor;
import br.com.escola.peopleservice.infra.persistence.PeopleWriteBackfillReport;

@Component
@ConditionalOnBean(PeopleWriteBackfillExecutor.class)
public class PeopleWriteBackfillRunner implements ApplicationRunner, Ordered {

    private final PeopleWriteBackfillExecutor executor;
    private final PeopleWriteBackfillProperties properties;

    public PeopleWriteBackfillRunner(
            PeopleWriteBackfillExecutor executor,
            PeopleWriteBackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        PeopleWriteBackfillReport report = executor.execute();
        if (properties.failOnMismatch() && !report.reconciled()) {
            throw new IllegalStateException("people-write-backfill-not-reconciled");
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
