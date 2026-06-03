package br.com.escola.catalogo.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.PeriodoLetivoOutput;
import br.com.escola.catalogo.application.port.out.PeriodoLetivoGateway;

@Service
public class ListarPeriodosLetivosUseCase {

    private final PeriodoLetivoGateway periodoLetivoGateway;

    public ListarPeriodosLetivosUseCase(PeriodoLetivoGateway periodoLetivoGateway) {
        this.periodoLetivoGateway = periodoLetivoGateway;
    }

    public List<PeriodoLetivoOutput> executar() {
        return periodoLetivoGateway.findAll();
    }
}
