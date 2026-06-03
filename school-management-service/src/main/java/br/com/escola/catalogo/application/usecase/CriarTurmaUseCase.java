package br.com.escola.catalogo.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.TurmaInput;
import br.com.escola.catalogo.application.dto.TurmaOutput;
import br.com.escola.catalogo.application.port.out.PeriodoLetivoGateway;
import br.com.escola.catalogo.application.port.out.SerieGateway;
import br.com.escola.catalogo.application.port.out.TurmaGateway;
import br.com.escola.catalogo.domain.exception.PeriodoLetivoNaoEncontradoException;
import br.com.escola.catalogo.domain.exception.SerieNaoEncontradaException;
import br.com.escola.catalogo.domain.exception.TurmaJaCadastradaException;

@Service
public class CriarTurmaUseCase {

    private final TurmaGateway turmaGateway;
    private final PeriodoLetivoGateway periodoLetivoGateway;
    private final SerieGateway serieGateway;

    public CriarTurmaUseCase(
            TurmaGateway turmaGateway,
            PeriodoLetivoGateway periodoLetivoGateway,
            SerieGateway serieGateway) {
        this.turmaGateway = turmaGateway;
        this.periodoLetivoGateway = periodoLetivoGateway;
        this.serieGateway = serieGateway;
    }

    public TurmaOutput executar(TurmaInput input) {
        if (!periodoLetivoGateway.existsById(input.periodoLetivoId())) {
            throw new PeriodoLetivoNaoEncontradoException(input.periodoLetivoId());
        }
        if (!serieGateway.existsById(input.serieId())) {
            throw new SerieNaoEncontradaException(input.serieId());
        }
        turmaGateway.findByCodigoAndPeriodoLetivoId(input.codigo(), input.periodoLetivoId())
                .ifPresent(turma -> {
                    throw new TurmaJaCadastradaException(input.codigo(), input.periodoLetivoId());
                });
        return turmaGateway.save(input);
    }
}
