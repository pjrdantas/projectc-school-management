package br.com.escola.identityaccessservice.infra.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.identityaccessservice.infra.config.BackfillProperties;

@Component
@Order(10)
@ConditionalOnProperty(
        name = "identity-access.persistence.backfill.enabled",
        havingValue = "true")
public class BackfillRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackfillRunner.class);

    private final BackfillExecutor executor;
    private final BackfillProperties properties;

    public BackfillRunner(BackfillExecutor executor, BackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        BackfillReport report = executor.execute();
        LOGGER.info("Backfill de acesso concluido: {}", report);
        if (!report.reconciled() && properties.failOnMismatch()) {
            throw new IllegalStateException("Backfill de acesso nao reconciliado");
        }
    }
}
