package br.com.escola.institutionaltenantservice.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import br.com.escola.institutionaltenantservice.infra.persistence.CargaEscolaExecutor;

@Configuration
@EnableConfigurationProperties(CargaEscolaProperties.class)
public class CargaEscolaConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "institutional-tenant.persistence.school-load.enabled",
            havingValue = "true")
    CargaEscolaExecutor cargaEscolaExecutor(
            CargaEscolaProperties properties,
            DataSource dataSource) {
        DriverManagerDataSource source = new DriverManagerDataSource(
                properties.sourceUrl(), properties.sourceUsername(), properties.sourcePassword());
        return new CargaEscolaExecutor(
                new JdbcTemplate(source),
                new JdbcTemplate(dataSource),
                properties.batchSize());
    }
}
