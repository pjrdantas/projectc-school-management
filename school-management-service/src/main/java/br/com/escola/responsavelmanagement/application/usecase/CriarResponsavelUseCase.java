package br.com.escola.responsavelmanagement.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.dto.ResponsavelInput;
import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelJaCadastradoException;

@Service
public class CriarResponsavelUseCase {

    private final ResponsavelCommandGateway responsavelCommandGateway;

    public CriarResponsavelUseCase(ResponsavelCommandGateway responsavelCommandGateway) {
        this.responsavelCommandGateway = responsavelCommandGateway;
    }

    public ResponsavelOutput executar(ResponsavelInput input) {
        if (responsavelCommandGateway.existsByCpf(input.cpf())) {
            throw new ResponsavelJaCadastradoException();
        }
        return responsavelCommandGateway.save(input);
    }
}
