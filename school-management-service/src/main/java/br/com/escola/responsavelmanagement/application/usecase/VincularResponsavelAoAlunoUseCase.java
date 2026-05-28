package br.com.escola.responsavelmanagement.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.dto.VinculoAlunoResponsavelInput;
import br.com.escola.responsavelmanagement.application.dto.VinculoAlunoResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.AlunoResponsavelVinculoGateway;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelQueryGateway;
import br.com.escola.responsavelmanagement.domain.exception.AlunoResponsavelVinculoDuplicadoException;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelNaoEncontradoException;
import br.com.escola.studentmanagement.application.port.out.AlunoQueryGateway;
import br.com.escola.studentmanagement.domain.exception.AlunoNaoEncontradoException;

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
