package br.com.escola.academiccatalog.application.usecase;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.PeriodoLetivoOutput;
import br.com.escola.academiccatalog.application.port.out.PeriodoLetivoGateway;
import br.com.escola.academiccatalog.domain.exception.PeriodoLetivoNaoEncontradoException;

@Service
public class BuscarPeriodoLetivoPorIdUseCase {

    private final PeriodoLetivoGateway periodoLetivoGateway;

    public BuscarPeriodoLetivoPorIdUseCase(PeriodoLetivoGateway periodoLetivoGateway) {
        this.periodoLetivoGateway = periodoLetivoGateway;
    }

    public PeriodoLetivoOutput executar(@NonNull Long id) {
        return periodoLetivoGateway.findById(id).orElseThrow(() -> new PeriodoLetivoNaoEncontradoException(id));
    }
}
