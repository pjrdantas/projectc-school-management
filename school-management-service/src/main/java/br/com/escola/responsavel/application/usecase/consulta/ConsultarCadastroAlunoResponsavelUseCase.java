package br.com.escola.responsavel.application.usecase.consulta;

import org.springframework.stereotype.Service;

import br.com.escola.responsavel.application.dto.consulta.ConsultaCadastralPageOutput;
import br.com.escola.responsavel.application.port.out.consulta.ConsultaCadastralGateway;

@Service
public class ConsultarCadastroAlunoResponsavelUseCase {

    private final ConsultaCadastralGateway consultaCadastralGateway;

    public ConsultarCadastroAlunoResponsavelUseCase(ConsultaCadastralGateway consultaCadastralGateway) {
        this.consultaCadastralGateway = consultaCadastralGateway;
    }

    public ConsultaCadastralPageOutput executar(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        return consultaCadastralGateway.consultar(nomeAluno, cpfAluno, nomeResponsavel, cpfResponsavel, page, size);
    }
}
