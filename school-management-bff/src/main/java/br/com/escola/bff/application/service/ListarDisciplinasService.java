package br.com.escola.bff.application.service;

import java.util.List;

import br.com.escola.bff.application.dto.DisciplinaQuery;
import br.com.escola.bff.application.dto.DisciplinaView;
import br.com.escola.bff.application.port.out.DisciplinaQueryPort;
import br.com.escola.bff.application.usecase.ListarDisciplinasUseCase;
import reactor.core.publisher.Mono;

public class ListarDisciplinasService implements ListarDisciplinasUseCase {

    private final DisciplinaQueryPort disciplinaQueryPort;

    public ListarDisciplinasService(DisciplinaQueryPort disciplinaQueryPort) {
        this.disciplinaQueryPort = disciplinaQueryPort;
    }

    @Override
    public Mono<List<DisciplinaView>> executar(DisciplinaQuery query) {
        return disciplinaQueryPort.listar(query);
    }
}

