package br.com.escola.enrollmentdocumentservice.infra.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class MatriculaDependenciasClientConfiguration {

    @Bean
    @Qualifier("matriculaAlunoRestClient")
    RestClient matriculaAlunoRestClient(AlunoMatriculaClientProperties properties) {
        return restClient(properties.baseUrl().toString(), properties.connectTimeout().toMillis(), properties.readTimeout().toMillis());
    }

    @Bean
    @Qualifier("matriculaCatalogoAcademicoRestClient")
    RestClient matriculaCatalogoAcademicoRestClient(CatalogoAcademicoMatriculaClientProperties properties) {
        return restClient(properties.baseUrl().toString(), properties.connectTimeout().toMillis(), properties.readTimeout().toMillis());
    }

    private RestClient restClient(String baseUrl, long connectTimeout, long readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.toIntExact(connectTimeout));
        factory.setReadTimeout(Math.toIntExact(readTimeout));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
}
