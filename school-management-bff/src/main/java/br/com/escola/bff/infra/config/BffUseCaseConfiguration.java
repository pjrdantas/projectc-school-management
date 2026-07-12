package br.com.escola.bff.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escola.bff.application.port.out.AcademicCatalogPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.AcademicCatalogSerieWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.AcademicCatalogTurnoResolverPort;
import br.com.escola.bff.application.port.out.AcademicCatalogTurmaDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogTurmaWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import br.com.escola.bff.application.port.out.MonolithDisciplinaWritePort;
import br.com.escola.bff.application.port.out.MonolithPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.MonolithSerieWritePort;
import br.com.escola.bff.application.port.out.MonolithTurmaDisciplinaWritePort;
import br.com.escola.bff.application.port.out.MonolithTurmaWritePort;
import br.com.escola.bff.application.port.out.PeopleFuncionarioReadPort;
import br.com.escola.bff.application.service.CatalogReadRoutingService;
import br.com.escola.bff.application.service.DisciplinaWriteRoutingService;
import br.com.escola.bff.application.service.FuncionarioReadProxyService;
import br.com.escola.bff.application.service.PeriodoLetivoWriteRoutingService;
import br.com.escola.bff.application.service.ProfessorReadProxyService;
import br.com.escola.bff.application.service.SerieWriteRoutingService;
import br.com.escola.bff.application.service.TurmaDisciplinaWriteRoutingService;
import br.com.escola.bff.application.service.TurmaWriteRoutingService;
import br.com.escola.bff.application.port.out.PeopleProfessorReadPort;
import br.com.escola.bff.application.usecase.ConsultarFuncionarioUseCase;
import br.com.escola.bff.application.usecase.ConsultarProfessorUseCase;
import br.com.escola.bff.application.usecase.CreateDisciplinaUseCase;
import br.com.escola.bff.application.usecase.CreatePeriodoLetivoUseCase;
import br.com.escola.bff.application.usecase.CreateSerieUseCase;
import br.com.escola.bff.application.usecase.CreateTurmaUseCase;
import br.com.escola.bff.application.usecase.LinkTurmaDisciplinaUseCase;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;

@Configuration
public class BffUseCaseConfiguration {

    @Bean
    ConsultarFuncionarioUseCase consultarFuncionarioUseCase(
            AuthContextPort authContextPort,
            PeopleFuncionarioReadPort peopleFuncionarioReadPort) {
        return new FuncionarioReadProxyService(authContextPort, peopleFuncionarioReadPort);
    }

    @Bean
    ConsultarProfessorUseCase consultarProfessorUseCase(
            AuthContextPort authContextPort,
            PeopleProfessorReadPort peopleProfessorReadPort) {
        return new ProfessorReadProxyService(authContextPort, peopleProfessorReadPort);
    }

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

    @Bean
    CreateTurmaUseCase createTurmaUseCase(
            MonolithTurmaWritePort monolithTurmaWritePort,
            AcademicCatalogTurmaWritePort academicCatalogTurmaWritePort,
            AcademicCatalogTurnoResolverPort academicCatalogTurnoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new TurmaWriteRoutingService(
                monolithTurmaWritePort,
                academicCatalogTurmaWritePort,
                academicCatalogTurnoResolverPort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    LinkTurmaDisciplinaUseCase linkTurmaDisciplinaUseCase(
            MonolithTurmaDisciplinaWritePort monolithTurmaDisciplinaWritePort,
            AcademicCatalogTurmaDisciplinaWritePort academicCatalogTurmaDisciplinaWritePort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new TurmaDisciplinaWriteRoutingService(
                monolithTurmaDisciplinaWritePort,
                academicCatalogTurmaDisciplinaWritePort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }
}
