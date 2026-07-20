package br.com.escola.identityaccessservice.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import br.com.escola.identityaccessservice.infra.persistence.BackfillExecutor;

@Configuration
@EnableConfigurationProperties(BackfillProperties.class)
public class BackfillConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "identity-access.persistence.backfill.enabled",
            havingValue = "true")
    BackfillExecutor backfillExecutor(
            BackfillProperties backfill,
            DataSource dataSource) {
        DriverManagerDataSource source = new DriverManagerDataSource(
                backfill.sourceUrl(), backfill.sourceUsername(), backfill.sourcePassword());
        return new BackfillExecutor(
                new JdbcTemplate(source),
                new JdbcTemplate(dataSource),
                backfill.batchSize());
    }
}
