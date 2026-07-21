package br.com.escola.responsiblesservice.infra.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

import br.com.escola.responsiblesservice.infra.persistence.ResponsiblesWriteBackfillExecutor;

@Configuration
public class ResponsiblesWriteBackfillConfiguration {

    @Bean
    @ConditionalOnProperty(name = "responsibles.persistence.backfill.enabled", havingValue = "true")
    ResponsiblesWriteBackfillExecutor responsiblesWriteBackfillExecutor(
            ResponsiblesWriteBackfillProperties backfillProperties,
            DataSource targetDataSource) {
        if (!StringUtils.hasText(backfillProperties.sourceUrl())) {
            throw new IllegalStateException("responsibles-write-backfill-source-url-required");
        }
        return new ResponsiblesWriteBackfillExecutor(
                new JdbcTemplate(new DriverManagerDataSource(
                        backfillProperties.sourceUrl(),
                        backfillProperties.sourceUsername(),
                        backfillProperties.sourcePassword())),
                new JdbcTemplate(targetDataSource),
                backfillProperties.batchSize());
    }
}
