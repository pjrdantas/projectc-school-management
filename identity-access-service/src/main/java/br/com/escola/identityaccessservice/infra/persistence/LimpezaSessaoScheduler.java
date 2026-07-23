package br.com.escola.identityaccessservice.infra.persistence;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.escola.identityaccessservice.infra.config.LimpezaSessaoProperties;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class LimpezaSessaoScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final LimpezaSessaoProperties properties;
    private final MeterRegistry meterRegistry;

    public LimpezaSessaoScheduler(
            JdbcTemplate jdbcTemplate,
            LimpezaSessaoProperties properties,
            MeterRegistry meterRegistry) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelayString = "${identity-access.session-cleanup.fixed-delay:24h}")
    public void executar() {
        if (!properties.enabled()) {
            return;
        }
        int removidas = executarEm(LocalDateTime.now());
        meterRegistry.counter("identity.session.cleanup", "result", "success")
                .increment(removidas);
    }

    int executarEm(LocalDateTime referencia) {
        return jdbcTemplate.update(
                "DELETE FROM sessao_autenticacao WHERE expira_em < ?",
                referencia);
    }
}
