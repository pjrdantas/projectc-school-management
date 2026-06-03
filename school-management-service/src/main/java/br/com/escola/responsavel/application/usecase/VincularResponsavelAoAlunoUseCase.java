package br.com.escola.responsavel.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.responsavel.application.dto.VinculoAlunoResponsavelInput;
import br.com.escola.responsavel.application.dto.VinculoAlunoResponsavelOutput;
import br.com.escola.responsavel.application.port.out.AlunoResponsavelVinculoGateway;
import br.com.escola.responsavel.application.port.out.ResponsavelQueryGateway;
import br.com.escola.responsavel.domain.exception.AlunoResponsavelVinculoDuplicadoException;
import br.com.escola.responsavel.domain.exception.ResponsavelNaoEncontradoException;
import br.com.escola.aluno.application.port.out.AlunoQueryGateway;
import br.com.escola.aluno.domain.exception.AlunoNaoEncontradoException;

@Service
public class VincularResponsavelAoAlunoUseCase {

    private final AlunoResponsavelVinculoGateway alunoResponsavelVinculoGateway;
    private final AlunoQueryGateway alunoQueryGateway;
    private final ResponsavelQueryGateway responsavelQueryGateway;

    public VincularResponsavelAoAlunoUseCase(
            AlunoResponsavelVinculoGateway alunoResponsavelVinculoGateway,
            AlunoQueryGateway alunoQueryGateway,
            ResponsavelQueryGateway responsavelQueryGateway) {
        this.alunoResponsavelVinculoGateway = alunoResponsavelVinculoGateway;
        this.alunoQueryGateway = alunoQueryGateway;
        this.responsavelQueryGateway = responsavelQueryGateway;
    }

    public VinculoAlunoResponsavelOutput executar(UUID idAluno, UUID idResponsavel) {
        return executar(new VinculoAlunoResponsavelInput(idAluno, idResponsavel, null, false, false, false));
    }

    public VinculoAlunoResponsavelOutput executar(VinculoAlunoResponsavelInput input) {
        UUID idAluno = input.idAluno();
        UUID idResponsavel = input.idResponsavel();

        if (!alunoQueryGateway.existsById(idAluno)) {
            throw new AlunoNaoEncontradoException(idAluno);
        }

        if (!responsavelQueryGateway.existsById(idResponsavel)) {
            throw new ResponsavelNaoEncontradoException(idResponsavel);
        }

        if (alunoResponsavelVinculoGateway.existsByAlunoAndResponsavel(idAluno, idResponsavel)) {
            throw new AlunoResponsavelVinculoDuplicadoException();
        }

        return alunoResponsavelVinculoGateway.save(input);
    }
}
