package br.com.escola.peopleservice.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class EscolaPessoaClientConfiguration {

    @Bean
    RestClient escolaPessoaRestClient(EscolaPessoaClientProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.toIntExact(properties.connectTimeout().toMillis()));
        factory.setReadTimeout(Math.toIntExact(properties.readTimeout().toMillis()));
        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(factory)
                .build();
    }
}
