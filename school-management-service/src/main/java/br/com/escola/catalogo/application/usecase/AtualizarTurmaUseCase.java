package br.com.escola.catalogo.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.TurmaInput;
import br.com.escola.catalogo.application.dto.TurmaOutput;
import br.com.escola.catalogo.application.port.out.PeriodoLetivoGateway;
import br.com.escola.catalogo.application.port.out.SerieGateway;
import br.com.escola.catalogo.application.port.out.TurmaGateway;
import br.com.escola.catalogo.domain.exception.PeriodoLetivoNaoEncontradoException;
import br.com.escola.catalogo.domain.exception.SerieNaoEncontradaException;
import br.com.escola.catalogo.domain.exception.TurmaCapacidadeInvalidaException;
import br.com.escola.catalogo.domain.exception.TurmaJaCadastradaException;
import br.com.escola.catalogo.domain.exception.TurmaNaoEncontradaException;
import br.com.escola.matricula.application.port.out.MatriculaGateway;

@Service
public class AtualizarTurmaUseCase {

    private final TurmaGateway turmaGateway;
    private final PeriodoLetivoGateway periodoLetivoGateway;
    private final SerieGateway serieGateway;
    private final MatriculaGateway matriculaGateway;

    public AtualizarTurmaUseCase(
            TurmaGateway turmaGateway,
            PeriodoLetivoGateway periodoLetivoGateway,
            SerieGateway serieGateway,
            MatriculaGateway matriculaGateway) {
        this.turmaGateway = turmaGateway;
        this.periodoLetivoGateway = periodoLetivoGateway;
        this.serieGateway = serieGateway;
        this.matriculaGateway = matriculaGateway;
    }

    public TurmaOutput executar(UUID id, TurmaInput input) {
        if (turmaGateway.findById(id).isEmpty()) {
            throw new TurmaNaoEncontradaException(id);
        }

        if (!periodoLetivoGateway.existsById(input.periodoLetivoId())) {
            throw new PeriodoLetivoNaoEncontradoException(input.periodoLetivoId());
        }
        if (!serieGateway.existsById(input.serieId())) {
            throw new SerieNaoEncontradaException(input.serieId());
        }

        turmaGateway.findByCodigoAndPeriodoLetivoId(input.codigo(), input.periodoLetivoId())
                .filter(turma -> !turma.id().equals(id))
                .ifPresent(turma -> {
                    throw new TurmaJaCadastradaException(input.codigo(), input.periodoLetivoId());
                });

        long matriculasQueOcupamVaga = matriculaGateway.countMatriculasQueOcupamVagaByTurmaId(id);
        if (input.capacidade() < matriculasQueOcupamVaga) {
            throw new TurmaCapacidadeInvalidaException(id, input.capacidade(), matriculasQueOcupamVaga);
        }

        return turmaGateway.update(id, input);
    }
}
