package br.com.escola.enrollmentdocumentservice.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import br.com.escola.enrollmentdocumentservice.infra.persistence.EnrollmentWriteBackfillExecutor;

@Configuration
public class EnrollmentWriteBackfillConfiguration {

    @Bean
    @ConditionalOnProperty(name = "enrollment-document.persistence.backfill.enabled", havingValue = "true")
    EnrollmentWriteBackfillExecutor enrollmentWriteBackfillExecutor(
            EnrollmentWriteBackfillProperties properties,
            DataSource targetDataSource) {
        if (!StringUtils.hasText(properties.sourceUrl())) {
            throw new IllegalStateException("enrollment-write-backfill-source-url-required");
        }
        return new EnrollmentWriteBackfillExecutor(
                new JdbcTemplate(new DriverManagerDataSource(
                        properties.sourceUrl(), properties.sourceUsername(), properties.sourcePassword())),
                new JdbcTemplate(targetDataSource),
                properties.batchSize());
    }
}
