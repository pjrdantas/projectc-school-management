package br.com.escola.responsavel.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.responsavel.application.dto.ResponsavelVinculadoOutput;
import br.com.escola.responsavel.application.port.out.AlunoResponsavelVinculoGateway;
import br.com.escola.responsavel.application.port.out.ResponsavelQueryGateway;
import br.com.escola.aluno.application.port.out.AlunoQueryGateway;
import br.com.escola.aluno.domain.exception.AlunoNaoEncontradoException;

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

    public List<ResponsavelVinculadoOutput> executar(UUID idAluno) {
        if (!alunoQueryGateway.existsById(idAluno)) {
            throw new AlunoNaoEncontradoException(idAluno);
        }

        return alunoResponsavelVinculoGateway.findByAluno(idAluno).stream()
                .map(vinculo -> responsavelQueryGateway.findById(vinculo.idResponsavel())
                        .map(responsavel -> ResponsavelVinculadoOutput.of(responsavel, vinculo))
                        .orElse(null))
                .filter(responsavelVinculado -> responsavelVinculado != null)
                .toList();
    }
}
