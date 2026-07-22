package br.com.escola.planningaiservice.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import br.com.escola.planningaiservice.infra.migration.PlanejamentoBackfillExecutor;

@Configuration
public class PlanejamentoBackfillConfiguration {
    @Bean
    @ConditionalOnProperty(name = "planning-ai.persistence.backfill.enabled", havingValue = "true")
    PlanejamentoBackfillExecutor planejamentoBackfillExecutor(PlanejamentoBackfillProperties properties, DataSource target) {
        if (!StringUtils.hasText(properties.sourceUrl())) {
            throw new IllegalStateException("planning-ai-backfill-source-url-required");
        }
        return new PlanejamentoBackfillExecutor(
                new JdbcTemplate(new DriverManagerDataSource(properties.sourceUrl(), properties.sourceUsername(), properties.sourcePassword())),
                new JdbcTemplate(target), properties.batchSize());
    }
}
