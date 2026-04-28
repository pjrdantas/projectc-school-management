package br.com.escola.responsavelmanagement.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.dto.ResponsavelInput;
import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelJaCadastradoException;

@Service
public class AtualizarResponsavelUseCase {

    private final ResponsavelCommandGateway responsavelCommandGateway;

    public AtualizarResponsavelUseCase(ResponsavelCommandGateway responsavelCommandGateway) {
        this.responsavelCommandGateway = responsavelCommandGateway;
    }

    public ResponsavelOutput executar(UUID id, ResponsavelInput input) {
        if (responsavelCommandGateway.existsByCpfAndIdNot(input.cpf(), id)) {
            throw new ResponsavelJaCadastradoException();
        }
        return responsavelCommandGateway.update(id, input);
    }
}
