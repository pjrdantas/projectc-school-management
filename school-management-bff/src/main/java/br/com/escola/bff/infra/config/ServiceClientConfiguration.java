package br.com.escola.bff.infra.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class ServiceClientConfiguration {

    @Bean
    WebClient catalogServiceWebClient(CatalogServiceClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient peopleServiceWebClient(CadastroPessoaClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient responsiblesServiceWebClient(ResponsavelCatalogoServiceClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient identityAccessServiceWebClient(AutenticacaoClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient institutionalTenantServiceWebClient(TenantAtivoServiceClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient dashboardQueryServiceWebClient(PainelQueryServiceClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient enrollmentDocumentServiceWebClient(DocumentoMatriculaServiceClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient pedagogicalServiceWebClient(EnsinoClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient planningAiServiceWebClient(PlanejamentoIaServiceClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    @Bean
    WebClient academicProfessorServiceWebClient(ProfessorServiceClientProperties properties) {
        return webClient(properties.baseUrl().toString(), properties.connectTimeout(), properties.responseTimeout());
    }

    private WebClient webClient(String baseUrl, Duration connectTimeout, Duration responseTimeout) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(connectTimeout.toMillis()))
                .responseTimeout(responseTimeout);
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
