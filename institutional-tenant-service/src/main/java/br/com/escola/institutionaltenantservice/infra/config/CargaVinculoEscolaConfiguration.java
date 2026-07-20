package br.com.escola.institutionaltenantservice.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import br.com.escola.institutionaltenantservice.infra.persistence.CargaVinculoEscolaExecutor;

@Configuration
@EnableConfigurationProperties(CargaVinculoEscolaProperties.class)
public class CargaVinculoEscolaConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "institutional-tenant.persistence.user-school-load.enabled",
            havingValue = "true")
    CargaVinculoEscolaExecutor cargaVinculoEscolaExecutor(
            CargaVinculoEscolaProperties properties,
            DataSource dataSource) {
        DriverManagerDataSource source = new DriverManagerDataSource(
                properties.sourceUrl(), properties.sourceUsername(), properties.sourcePassword());
        return new CargaVinculoEscolaExecutor(
                new JdbcTemplate(source),
                new JdbcTemplate(dataSource),
                properties.batchSize());
    }
}
