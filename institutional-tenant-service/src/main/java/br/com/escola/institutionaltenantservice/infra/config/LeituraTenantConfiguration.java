package br.com.escola.institutionaltenantservice.infra.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(LeituraTenantProperties.class)
public class LeituraTenantConfiguration {

    @Bean("tenantReadDataSource")
    DataSource tenantReadDataSource(LeituraTenantProperties properties) {
        return new DriverManagerDataSource(properties.url(), properties.username(), properties.password());
    }

    @Bean("tenantReadJdbcTemplate")
    JdbcTemplate tenantReadJdbcTemplate(
            @Qualifier("tenantReadDataSource") DataSource tenantReadDataSource) {
        return new JdbcTemplate(tenantReadDataSource);
    }

    @Bean("tenantTransactionManager")
    PlatformTransactionManager tenantTransactionManager(
            @Qualifier("tenantReadDataSource") DataSource tenantReadDataSource) {
        return new DataSourceTransactionManager(tenantReadDataSource);
    }
}
