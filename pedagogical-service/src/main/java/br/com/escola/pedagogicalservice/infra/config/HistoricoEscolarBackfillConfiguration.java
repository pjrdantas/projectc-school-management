package br.com.escola.pedagogicalservice.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.pedagogicalservice.infra.persistence.HistoricoEscolarBackfillExecutor;

@Configuration
public class HistoricoEscolarBackfillConfiguration {

    @Bean
    @ConditionalOnProperty(name = "pedagogical.persistence.backfill.enabled", havingValue = "true")
    HistoricoEscolarBackfillExecutor historicoEscolarBackfillExecutor(
            HistoricoEscolarBackfillProperties properties,
            DataSource targetDataSource,
            ObjectMapper objectMapper) {
        if (!StringUtils.hasText(properties.sourceUrl()) || properties.schoolId() == null) {
            throw new IllegalStateException("pedagogical-history-backfill-source-and-school-required");
        }
        return new HistoricoEscolarBackfillExecutor(
                new JdbcTemplate(new DriverManagerDataSource(
                        properties.sourceUrl(), properties.sourceUsername(), properties.sourcePassword())),
                new JdbcTemplate(targetDataSource), objectMapper, properties.schoolId(), properties.batchSize());
    }
}
