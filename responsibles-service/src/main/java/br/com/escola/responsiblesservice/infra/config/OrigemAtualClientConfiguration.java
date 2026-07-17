package br.com.escola.responsiblesservice.infra.config;

import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OrigemAtualClientConfiguration {

    @Bean
    RestClient responsiblesMonolithRestClient(
            RestClient.Builder builder,
            RestClientBuilderConfigurer configurer,
            OrigemAtualClientProperties properties) {
        return configurer.configure(builder)
                .baseUrl(properties.baseUrl().toString())
                .build();
    }
}

