package br.com.escola.bff.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escola.bff.application.port.out.AcademicCatalogPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import br.com.escola.bff.application.port.out.MonolithPeriodoLetivoWritePort;
import br.com.escola.bff.application.service.CatalogReadRoutingService;
import br.com.escola.bff.application.service.PeriodoLetivoWriteRoutingService;
import br.com.escola.bff.application.usecase.CreatePeriodoLetivoUseCase;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;

@Configuration
public class BffUseCaseConfiguration {

    @Bean
    RouteCatalogReadUseCase routeCatalogReadUseCase(
            MonolithCatalogReadPort monolithCatalogReadPort,
            AcademicCatalogReadPort academicCatalogReadPort,
            AuthContextPort authContextPort,
            CatalogReadCutoverPolicyPort cutoverPolicyPort,
            CatalogReadObservabilityPort observabilityPort) {
        return new CatalogReadRoutingService(
                monolithCatalogReadPort,
                academicCatalogReadPort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    CreatePeriodoLetivoUseCase createPeriodoLetivoUseCase(
            MonolithPeriodoLetivoWritePort monolithPeriodoLetivoWritePort,
            AcademicCatalogPeriodoLetivoWritePort academicCatalogPeriodoLetivoWritePort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new PeriodoLetivoWriteRoutingService(
                monolithPeriodoLetivoWritePort,
                academicCatalogPeriodoLetivoWritePort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }
}
