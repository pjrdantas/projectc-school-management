package br.com.escola.responsavelmanagement.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.AlunoResponsavelVinculoGateway;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelQueryGateway;
import br.com.escola.studentmanagement.application.port.out.AlunoQueryGateway;
import br.com.escola.studentmanagement.domain.exception.AlunoNaoEncontradoException;

@Service
public class ListarResponsaveisPorAlunoUseCase {

    private final AlunoResponsavelVinculoGateway alunoResponsavelVinculoGateway;
    private final ResponsavelQueryGateway responsavelQueryGateway;
    private final AlunoQueryGateway alunoQueryGateway;

    public ListarResponsaveisPorAlunoUseCase(
            AlunoResponsavelVinculoGateway alunoResponsavelVinculoGateway,
            ResponsavelQueryGateway responsavelQueryGateway,
            AlunoQueryGateway alunoQueryGateway) {
        this.alunoResponsavelVinculoGateway = alunoResponsavelVinculoGateway;
        this.responsavelQueryGateway = responsavelQueryGateway;
        this.alunoQueryGateway = alunoQueryGateway;
    }

    @SuppressWarnings("null")
	public List<ResponsavelOutput> executar(@NonNull UUID idAluno) {
        if (!alunoQueryGateway.existsById(idAluno)) {
            throw new AlunoNaoEncontradoException(idAluno);
        }

        return alunoResponsavelVinculoGateway.findByAluno(idAluno).stream()
                .map(vinculo -> responsavelQueryGateway.findById(vinculo.idResponsavel()).orElse(null))
                .filter(responsavel -> responsavel != null)
                .toList();
    }
}
