package br.com.escola.dashboardqueryservice.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import br.com.escola.dashboardqueryservice.infra.persistence.PainelBackfillExecutor;

@Configuration
@EnableConfigurationProperties(PainelBackfillProperties.class)
public class PainelBackfillConfiguration {

    @Bean
    @ConditionalOnProperty(name = "dashboard-query.persistence.backfill.enabled", havingValue = "true")
    PainelBackfillExecutor painelBackfillExecutor(PainelBackfillProperties properties, DataSource targetDataSource) {
        if (!StringUtils.hasText(properties.sourceUrl()) || properties.escolaId() == null) {
            throw new IllegalStateException("dashboard-query-backfill-source-url-and-escola-id-required");
        }
        return new PainelBackfillExecutor(
                new JdbcTemplate(new DriverManagerDataSource(
                        properties.sourceUrl(), properties.sourceUsername(), properties.sourcePassword())),
                new JdbcTemplate(targetDataSource), properties.escolaId(), properties.batchSize());
    }
}
