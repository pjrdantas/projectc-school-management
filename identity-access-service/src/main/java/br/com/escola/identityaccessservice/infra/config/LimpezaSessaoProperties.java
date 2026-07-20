package br.com.escola.identityaccessservice.infra.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity-access.session-cleanup")
public record LimpezaSessaoProperties(boolean enabled, Duration fixedDelay) {

    public LimpezaSessaoProperties {
        fixedDelay = fixedDelay == null ? Duration.ofHours(24) : fixedDelay;
    }
}
