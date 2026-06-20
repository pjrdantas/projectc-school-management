package br.com.escola.catalog.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.com.escola.catalog.infra.security.InternalApiInterceptor;

@Configuration
public class InternalApiWebConfiguration implements WebMvcConfigurer {

    private final InternalApiInterceptor internalApiInterceptor;

    public InternalApiWebConfiguration(InternalApiInterceptor internalApiInterceptor) {
        this.internalApiInterceptor = internalApiInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiInterceptor).addPathPatterns("/internal/v1/**");
    }
}
