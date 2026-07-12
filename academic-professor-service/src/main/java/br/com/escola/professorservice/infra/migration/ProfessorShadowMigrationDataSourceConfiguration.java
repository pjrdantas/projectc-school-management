package br.com.escola.professorservice.infra.migration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ConditionalOnProperty(name = "professor.shadow.migration.enabled", havingValue = "true")
public class ProfessorShadowMigrationDataSourceConfiguration {

    @Bean
    ProfessorShadowMigrationSourceConnection professorShadowMigrationSourceConnection(
            @Value("${professor.shadow.migration.source.url}") String url,
            @Value("${professor.shadow.migration.source.username}") String username,
            @Value("${professor.shadow.migration.source.password}") String password,
            @Value("${professor.shadow.migration.source.driver-class-name:org.postgresql.Driver}") String driverClassName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        transaction.setReadOnly(true);
        transaction.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        return new ProfessorShadowMigrationSourceConnection(jdbc, transaction);
    }
}
