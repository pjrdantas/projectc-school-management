package br.com.escola.professorservice.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ApoioClientConfiguration {

    @Bean
    RestClient pessoaApoioRestClient(PessoaApoioClientProperties properties) {
        return build(properties.baseUrl().toString(), properties.connectTimeout().toMillis(), properties.readTimeout().toMillis());
    }

    @Bean
    RestClient catalogoApoioRestClient(CatalogoApoioClientProperties properties) {
        return build(properties.baseUrl().toString(), properties.connectTimeout().toMillis(), properties.readTimeout().toMillis());
    }

    private RestClient build(String baseUrl, long connectTimeoutMs, long readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.toIntExact(connectTimeoutMs));
        factory.setReadTimeout(Math.toIntExact(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
