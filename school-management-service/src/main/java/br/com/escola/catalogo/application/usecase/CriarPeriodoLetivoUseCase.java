package br.com.escola.catalogo.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.PeriodoLetivoInput;
import br.com.escola.catalogo.application.dto.PeriodoLetivoOutput;
import br.com.escola.catalogo.application.port.out.PeriodoLetivoGateway;
import br.com.escola.catalogo.domain.exception.PeriodoLetivoInvalidoException;

@Service
public class CriarPeriodoLetivoUseCase {

    private final PeriodoLetivoGateway periodoLetivoGateway;

    public CriarPeriodoLetivoUseCase(PeriodoLetivoGateway periodoLetivoGateway) {
        this.periodoLetivoGateway = periodoLetivoGateway;
    }

    public PeriodoLetivoOutput executar(PeriodoLetivoInput input) {
        if (input.dataFim().isBefore(input.dataInicio())) {
            throw new PeriodoLetivoInvalidoException();
        }
        return periodoLetivoGateway.save(input);
    }
}