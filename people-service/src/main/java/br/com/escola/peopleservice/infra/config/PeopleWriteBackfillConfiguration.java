package br.com.escola.peopleservice.infra.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.infra.persistence.PeopleWriteBackfillExecutor;

@Configuration
public class PeopleWriteBackfillConfiguration {

    @Bean
    @ConditionalOnProperty(name = "people.read-model.backfill.enabled", havingValue = "true")
    PeopleWriteBackfillExecutor peopleWriteBackfillExecutor(
            PeopleWriteBackfillProperties backfillProperties,
            LeituraModeloMigrationProperties targetProperties) {
        if (!StringUtils.hasText(backfillProperties.sourceUrl())) {
            throw new IllegalStateException("people-write-backfill-source-url-required");
        }
        return new PeopleWriteBackfillExecutor(
                new JdbcTemplate(new DriverManagerDataSource(
                        backfillProperties.sourceUrl(),
                        backfillProperties.sourceUsername(),
                        backfillProperties.sourcePassword())),
                new JdbcTemplate(new DriverManagerDataSource(
                        targetProperties.url(), targetProperties.username(), targetProperties.password())),
                backfillProperties.batchSize());
    }
}
