package br.com.escola.catalogo.application.usecase;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.PeriodoLetivoOutput;
import br.com.escola.catalogo.application.port.out.PeriodoLetivoGateway;
import br.com.escola.catalogo.domain.exception.PeriodoLetivoNaoEncontradoException;

@Service
public class BuscarPeriodoLetivoPorIdUseCase {

    private final PeriodoLetivoGateway periodoLetivoGateway;

    public BuscarPeriodoLetivoPorIdUseCase(PeriodoLetivoGateway periodoLetivoGateway) {
        this.periodoLetivoGateway = periodoLetivoGateway;
    }

    public PeriodoLetivoOutput executar(@NonNull UUID id) {
        return periodoLetivoGateway.findById(id).orElseThrow(() -> new PeriodoLetivoNaoEncontradoException(id));
    }
}
