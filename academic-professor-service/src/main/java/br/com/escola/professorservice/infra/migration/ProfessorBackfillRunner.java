package br.com.escola.professorservice.infra.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.professorservice.infra.config.ProfessorBackfillProperties;
import br.com.escola.professorservice.infra.persistence.ProfessorBackfillExecutor;

@Component
@ConditionalOnBean(ProfessorBackfillExecutor.class)
@Order(1)
public class ProfessorBackfillRunner implements ApplicationRunner {

    private final ProfessorBackfillExecutor executor;
    private final ProfessorBackfillProperties properties;

    public ProfessorBackfillRunner(ProfessorBackfillExecutor executor, ProfessorBackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.failOnMismatch() && !executor.execute().reconciled()) {
            throw new IllegalStateException("academic-professor-backfill-not-reconciled");
        }
    }
}
