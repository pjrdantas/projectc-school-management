package br.com.escola.responsavelmanagement.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.port.out.AlunoResponsavelVinculoGateway;
import br.com.escola.responsavelmanagement.domain.exception.AlunoResponsavelVinculoNaoEncontradoException;

@Service
public class DesvincularResponsavelDoAlunoUseCase {

    private final AlunoResponsavelVinculoGateway alunoResponsavelVinculoGateway;

    public DesvincularResponsavelDoAlunoUseCase(AlunoResponsavelVinculoGateway alunoResponsavelVinculoGateway) {
        this.alunoResponsavelVinculoGateway = alunoResponsavelVinculoGateway;
    }

    public void executar(UUID idAluno, UUID idResponsavel) {
        if (!alunoResponsavelVinculoGateway.existsByAlunoAndResponsavel(idAluno, idResponsavel)) {
            throw new AlunoResponsavelVinculoNaoEncontradoException(idAluno, idResponsavel);
        }

        alunoResponsavelVinculoGateway.deleteByAlunoAndResponsavel(idAluno, idResponsavel);
    }
}
