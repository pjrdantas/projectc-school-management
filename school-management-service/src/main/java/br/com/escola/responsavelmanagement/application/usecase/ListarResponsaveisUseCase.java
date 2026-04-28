package br.com.escola.responsavelmanagement.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelQueryGateway;

@Service
public class ListarResponsaveisUseCase {

    private final ResponsavelQueryGateway responsavelQueryGateway;

    public ListarResponsaveisUseCase(ResponsavelQueryGateway responsavelQueryGateway) {
        this.responsavelQueryGateway = responsavelQueryGateway;
    }

    public List<ResponsavelOutput> executar() {
        return responsavelQueryGateway.findAll();
    }
}
