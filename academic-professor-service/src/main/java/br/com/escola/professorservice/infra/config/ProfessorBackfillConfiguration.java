package br.com.escola.professorservice.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import br.com.escola.professorservice.infra.persistence.ProfessorBackfillExecutor;

@Configuration
public class ProfessorBackfillConfiguration {

    @Bean
    @ConditionalOnProperty(name = "professor.persistence.backfill.enabled", havingValue = "true")
    ProfessorBackfillExecutor professorBackfillExecutor(
            ProfessorBackfillProperties properties,
            DataSource targetDataSource) {
        if (!StringUtils.hasText(properties.sourceUrl())) {
            throw new IllegalStateException("academic-professor-backfill-source-url-required");
        }
        return new ProfessorBackfillExecutor(
                new JdbcTemplate(new DriverManagerDataSource(
                        properties.sourceUrl(), properties.sourceUsername(), properties.sourcePassword())),
                new JdbcTemplate(targetDataSource),
                properties.batchSize());
    }
}
