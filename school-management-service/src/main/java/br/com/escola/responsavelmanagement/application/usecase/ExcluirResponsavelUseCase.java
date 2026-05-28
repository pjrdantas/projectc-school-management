package br.com.escola.responsavelmanagement.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelQueryGateway;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelComAlunoVinculadoException;
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

    public void executar(UUID id) {
        if (!responsavelQueryGateway.existsById(id)) {
            throw new ResponsavelNaoEncontradoException(id);
        }
        if (responsavelQueryGateway.hasAlunosVinculados(id)) {
            throw new ResponsavelComAlunoVinculadoException(id);
        }
        responsavelCommandGateway.deleteById(id);
    }
}
