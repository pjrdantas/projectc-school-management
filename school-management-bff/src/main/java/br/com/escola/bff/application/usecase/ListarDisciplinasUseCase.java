package br.com.escola.bff.application.usecase;

import java.util.List;

import br.com.escola.bff.application.dto.DisciplinaQuery;
import br.com.escola.bff.application.dto.DisciplinaView;
import reactor.core.publisher.Mono;

public interface ListarDisciplinasUseCase {

    Mono<List<DisciplinaView>> executar(DisciplinaQuery query);
}

