package br.com.escola.enrollmentdocumentservice.infra.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.enrollmentdocumentservice.infra.config.EnrollmentWriteBackfillProperties;
import br.com.escola.enrollmentdocumentservice.infra.persistence.EnrollmentWriteBackfillExecutor;

@Component
@ConditionalOnBean(EnrollmentWriteBackfillExecutor.class)
@Order(1)
public class EnrollmentWriteBackfillRunner implements ApplicationRunner {

    private final EnrollmentWriteBackfillExecutor executor;
    private final EnrollmentWriteBackfillProperties properties;

    public EnrollmentWriteBackfillRunner(
            EnrollmentWriteBackfillExecutor executor,
            EnrollmentWriteBackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.failOnMismatch() && !executor.execute().reconciled()) {
            throw new IllegalStateException("enrollment-write-backfill-not-reconciled");
        }
    }
}
