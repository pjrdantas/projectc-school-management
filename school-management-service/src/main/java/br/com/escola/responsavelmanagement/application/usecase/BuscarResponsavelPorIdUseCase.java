package br.com.escola.responsavelmanagement.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelQueryGateway;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelNaoEncontradoException;

@Service
public class BuscarResponsavelPorIdUseCase {

    private final ResponsavelQueryGateway responsavelQueryGateway;

    public BuscarResponsavelPorIdUseCase(ResponsavelQueryGateway responsavelQueryGateway) {
        this.responsavelQueryGateway = responsavelQueryGateway;
    }

    public ResponsavelOutput executar(UUID id) {
        return responsavelQueryGateway.findById(id)
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(id));
    }
}
