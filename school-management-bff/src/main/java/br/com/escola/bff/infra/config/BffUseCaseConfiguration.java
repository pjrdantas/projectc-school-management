package br.com.escola.bff.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import br.com.escola.bff.application.service.CatalogReadRoutingService;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;

@Configuration
public class BffUseCaseConfiguration {

    @Bean
    RouteCatalogReadUseCase routeCatalogReadUseCase(
            MonolithCatalogReadPort monolithCatalogReadPort,
            AcademicCatalogReadPort academicCatalogReadPort,
            AuthContextPort authContextPort,
            CatalogReadCutoverPolicyPort cutoverPolicyPort) {
        return new CatalogReadRoutingService(
                monolithCatalogReadPort,
                academicCatalogReadPort,
                authContextPort,
                cutoverPolicyPort);
    }
}
