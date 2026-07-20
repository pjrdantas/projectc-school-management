package br.com.escola.institutionaltenantservice.infra.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.institutionaltenantservice.infra.config.CargaVinculoEscolaProperties;

@Component
@Order(10)
@ConditionalOnProperty(
        name = "institutional-tenant.persistence.user-school-load.enabled",
        havingValue = "true")
public class CargaVinculoEscolaRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CargaVinculoEscolaRunner.class);

    private final CargaVinculoEscolaExecutor executor;
    private final CargaVinculoEscolaProperties properties;

    public CargaVinculoEscolaRunner(
            CargaVinculoEscolaExecutor executor,
            CargaVinculoEscolaProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        CargaVinculoEscolaReport report = executor.execute();
        LOGGER.info("Carga de vinculos usuario-escola concluida: {}", report);
        if (!report.reconciled() && properties.failOnMismatch()) {
            throw new IllegalStateException("Carga de vinculos usuario-escola nao reconciliada");
        }
    }
}
