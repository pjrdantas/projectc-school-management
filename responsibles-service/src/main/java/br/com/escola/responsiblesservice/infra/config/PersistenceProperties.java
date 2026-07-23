package br.com.escola.responsiblesservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record PersistenceProperties(
        String url,
        String username,
        String password,
        String driverClassName) {

    public PersistenceProperties {
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }
        if (driverClassName == null) {
            driverClassName = "";
        }
    }
}
