package br.com.escola.bff.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escola.bff.application.port.out.DisciplinaQueryPort;
import br.com.escola.bff.application.service.ListarDisciplinasService;
import br.com.escola.bff.application.usecase.ListarDisciplinasUseCase;

@Configuration
public class BffUseCaseConfiguration {

    @Bean
    ListarDisciplinasUseCase listarDisciplinasUseCase(DisciplinaQueryPort disciplinaQueryPort) {
        return new ListarDisciplinasService(disciplinaQueryPort);
    }
}

