package br.com.escola.responsavelmanagement.application.usecase;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelQueryGateway;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelNaoEncontradoException;

@Service
public class ExcluirResponsavelUseCase {

    private final ResponsavelCommandGateway responsavelCommandGateway;
    private final ResponsavelQueryGateway responsavelQueryGateway;

    public ExcluirResponsavelUseCase(
            ResponsavelCommandGateway responsavelCommandGateway,
            ResponsavelQueryGateway responsavelQueryGateway) {
        this.responsavelCommandGateway = responsavelCommandGateway;
        this.responsavelQueryGateway = responsavelQueryGateway;
    }

    public void executar(@NonNull UUID id) {
        if (!responsavelQueryGateway.existsById(id)) {
            throw new ResponsavelNaoEncontradoException(id);
        }
        responsavelCommandGateway.deleteById(id);
    }
}
