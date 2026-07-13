package br.com.escola.pedagogicalservice.infra.config;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class MonolithPedagogicalClientConfiguration {

    @Bean
    RestClient monolithPedagogicalRestClient(MonolithPedagogicalClientProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();

        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
