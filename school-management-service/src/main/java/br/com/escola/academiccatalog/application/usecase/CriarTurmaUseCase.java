package br.com.escola.academiccatalog.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.TurmaInput;
import br.com.escola.academiccatalog.application.dto.TurmaOutput;
import br.com.escola.academiccatalog.application.port.out.PeriodoLetivoGateway;
import br.com.escola.academiccatalog.application.port.out.TurmaGateway;
import br.com.escola.academiccatalog.domain.exception.PeriodoLetivoNaoEncontradoException;
import br.com.escola.academiccatalog.domain.exception.TurmaJaCadastradaException;

@Service
public class CriarTurmaUseCase {

    private final TurmaGateway turmaGateway;
    private final PeriodoLetivoGateway periodoLetivoGateway;

    public CriarTurmaUseCase(TurmaGateway turmaGateway, PeriodoLetivoGateway periodoLetivoGateway) {
        this.turmaGateway = turmaGateway;
        this.periodoLetivoGateway = periodoLetivoGateway;
    }

    @SuppressWarnings("null")
	public TurmaOutput executar(TurmaInput input) {
        if (!periodoLetivoGateway.existsById(input.periodoLetivoId())) {
            throw new PeriodoLetivoNaoEncontradoException(input.periodoLetivoId());
        }
        turmaGateway.findByCodigoAndPeriodoLetivoId(input.codigo(), input.periodoLetivoId())
                .ifPresent(turma -> {
                    throw new TurmaJaCadastradaException(input.codigo(), input.periodoLetivoId());
                });
        return turmaGateway.save(input);
    }
}