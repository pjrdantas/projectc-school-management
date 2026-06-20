package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.application.dto.DisciplinaQuery;
import br.com.escola.bff.application.dto.DisciplinaView;
import br.com.escola.bff.application.port.out.DisciplinaQueryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ListarDisciplinasServiceTest {

    @Test
    void deveDelegarConsultaParaPorta() {
        DisciplinaView disciplina = new DisciplinaView(
                UUID.randomUUID(), "Matematica", 80, "ATIVA",
                UUID.randomUUID(), "Escola", LocalDateTime.now());
        DisciplinaQueryPort port = query -> Mono.just(List.of(disciplina));
        ListarDisciplinasService service = new ListarDisciplinasService(port);

        StepVerifier.create(service.executar(new DisciplinaQuery("Bearer token", "corr-1")))
                .assertNext(result -> assertThat(result).containsExactly(disciplina))
                .verifyComplete();
    }
}

