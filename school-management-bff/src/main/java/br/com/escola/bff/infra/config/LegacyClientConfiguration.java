package br.com.escola.bff.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import reactor.netty.http.client.HttpClient;

@Configuration
public class LegacyClientConfiguration {

    @Bean
    WebClient monolithWebClient(LegacyClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient catalogServiceWebClient(CatalogServiceClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient peopleServiceWebClient(CadastroPessoaClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient responsiblesServiceWebClient(ResponsavelCatalogoServiceClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient identityAccessServiceWebClient(AutenticacaoClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient institutionalTenantServiceWebClient(TenantAtivoServiceClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient dashboardQueryServiceWebClient(PainelQueryServiceClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient enrollmentDocumentServiceWebClient(DocumentoMatriculaServiceClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient pedagogicalServiceWebClient(EnsinoClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    WebClient planningAiServiceWebClient(PlanejamentoIaServiceClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry(LegacyCircuitBreakerProperties properties) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.failureRateThreshold())
                .minimumNumberOfCalls(properties.minimumNumberOfCalls())
                .slidingWindowSize(properties.slidingWindowSize())
                .waitDurationInOpenState(properties.openStateDuration())
                .ignoreException(error -> error instanceof br.com.escola.bff.application.exception.DownstreamRejectedException)
                .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    CircuitBreaker monolithCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("monolith");
    }

    @Bean
    MeterBinder circuitBreakerMetrics(CircuitBreakerRegistry registry) {
        return TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry);
    }
}


