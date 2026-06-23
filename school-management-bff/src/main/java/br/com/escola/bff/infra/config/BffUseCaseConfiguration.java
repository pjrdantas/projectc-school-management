package br.com.escola.bff.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escola.bff.application.port.out.AcademicCatalogPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.AcademicCatalogSerieWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import br.com.escola.bff.application.port.out.MonolithDisciplinaWritePort;
import br.com.escola.bff.application.port.out.MonolithPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.MonolithSerieWritePort;
import br.com.escola.bff.application.service.CatalogReadRoutingService;
import br.com.escola.bff.application.service.DisciplinaWriteRoutingService;
import br.com.escola.bff.application.service.PeriodoLetivoWriteRoutingService;
import br.com.escola.bff.application.service.SerieWriteRoutingService;
import br.com.escola.bff.application.usecase.CreateDisciplinaUseCase;
import br.com.escola.bff.application.usecase.CreatePeriodoLetivoUseCase;
import br.com.escola.bff.application.usecase.CreateSerieUseCase;
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

    @Bean
    CreateDisciplinaUseCase createDisciplinaUseCase(
            MonolithDisciplinaWritePort monolithDisciplinaWritePort,
            AcademicCatalogDisciplinaWritePort academicCatalogDisciplinaWritePort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new DisciplinaWriteRoutingService(
                monolithDisciplinaWritePort,
                academicCatalogDisciplinaWritePort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    CreateSerieUseCase createSerieUseCase(
            MonolithSerieWritePort monolithSerieWritePort,
            AcademicCatalogSerieWritePort academicCatalogSerieWritePort,
            AcademicCatalogNivelEnsinoResolverPort academicCatalogNivelEnsinoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new SerieWriteRoutingService(
                monolithSerieWritePort,
                academicCatalogSerieWritePort,
                academicCatalogNivelEnsinoResolverPort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }
}
