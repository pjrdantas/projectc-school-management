package br.com.escola.responsiblesservice.infra.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class StudentClientConfiguration {

    @Bean
    @Qualifier("peopleStudentRestClient")
    RestClient peopleStudentRestClient(StudentClientProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.toIntExact(properties.connectTimeout().toMillis()));
        factory.setReadTimeout(Math.toIntExact(properties.readTimeout().toMillis()));
        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(factory)
                .build();
    }
}
