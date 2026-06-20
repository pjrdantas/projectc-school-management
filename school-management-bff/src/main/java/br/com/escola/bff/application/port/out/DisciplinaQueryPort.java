package br.com.escola.bff.application.port.out;

import java.util.List;

import br.com.escola.bff.application.dto.DisciplinaQuery;
import br.com.escola.bff.application.dto.DisciplinaView;
import reactor.core.publisher.Mono;

public interface DisciplinaQueryPort {

    Mono<List<DisciplinaView>> listar(DisciplinaQuery query);
}

