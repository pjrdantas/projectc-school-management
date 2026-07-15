package br.com.escola.dashboardqueryservice.infra.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard-query.monolith")
public record DashboardQueryMonolithClientProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {}
