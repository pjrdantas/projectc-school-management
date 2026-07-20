package br.com.escola.institutionaltenantservice.infra.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.institutionaltenantservice.infra.config.CargaEscolaProperties;

@Component
@Order(0)
@ConditionalOnProperty(
        name = "institutional-tenant.persistence.school-load.enabled",
        havingValue = "true")
public class CargaEscolaRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CargaEscolaRunner.class);

    private final CargaEscolaExecutor executor;
    private final CargaEscolaProperties properties;

    public CargaEscolaRunner(CargaEscolaExecutor executor, CargaEscolaProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        CargaEscolaReport report = executor.execute();
        LOGGER.info("Carga de escolas concluida: {}", report);
        if (!report.reconciled() && properties.failOnMismatch()) {
            throw new IllegalStateException("Carga de escolas nao reconciliada");
        }
    }
}
