package br.com.escola.institutionaltenantservice.infra.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(InstitutionalTenantMonolithClientProperties.class)
public class InstitutionalTenantMonolithClientConfiguration {

    @Bean
    RestClient institutionalTenantMonolithRestClient(InstitutionalTenantMonolithClientProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) Duration.ofSeconds(properties.connectTimeout().toSeconds()).toMillis());
                    setReadTimeout((int) Duration.ofSeconds(properties.readTimeout().toSeconds()).toMillis());
                }})
                .build();
    }
}
