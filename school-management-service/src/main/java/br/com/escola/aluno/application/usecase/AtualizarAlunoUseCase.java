package br.com.escola.aluno.application.usecase;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.aluno.application.dto.AlunoInput;
import br.com.escola.aluno.application.dto.AlunoOutput;
import br.com.escola.aluno.application.port.out.AlunoCommandGateway;
import br.com.escola.aluno.application.port.out.AlunoQueryGateway;
import br.com.escola.compartilhado.viacep.ViaCepResponse;
import br.com.escola.compartilhado.viacep.ViaCepService;
import br.com.escola.aluno.domain.exception.AlunoJaCadastradoException;
import br.com.escola.aluno.domain.exception.AlunoNaoEncontradoException;

@Service
public class AtualizarAlunoUseCase {

    private final AlunoCommandGateway alunoCommandGateway;
    private final AlunoQueryGateway alunoQueryGateway;
    private final ViaCepService viaCepService;

    public AtualizarAlunoUseCase(
            AlunoCommandGateway alunoCommandGateway,
            AlunoQueryGateway alunoQueryGateway,
            ViaCepService viaCepService) {
        this.alunoCommandGateway = alunoCommandGateway;
        this.alunoQueryGateway = alunoQueryGateway;
        this.viaCepService = viaCepService;
    }

    public AlunoOutput executar(@NonNull UUID id, AlunoInput input) {
        if (!alunoQueryGateway.existsById(id)) {
            throw new AlunoNaoEncontradoException(id);
        }

        if (alunoCommandGateway.existsByCpfAndIdNot(input.cpf(), input.escolaId(), id)) {
            throw new AlunoJaCadastradoException();
        }

        return alunoCommandGateway.update(id, preencherEndereco(input));
    }

    private AlunoInput preencherEndereco(AlunoInput input) {
        ViaCepResponse endereco = viaCepService.consultar(input.cep());
        if (endereco == null) {
            return input;
        }
        validarNumero(input.numero());
        return new AlunoInput(
                input.nomeCompleto(),
                input.cpf(),
                input.email(),
                input.telefone(),
                input.dataNascimento(),
                input.rg(),
                input.orgaoEmissorRg(),
                input.ufRg(),
                input.nacionalidade(),
                input.naturalidade(),
                input.sexo(),
                input.nomeSocial(),
                viaCepService.normalizar(input.cep()),
                endereco.logradouro(),
                input.numero(),
                input.complemento(),
                endereco.bairro(),
                endereco.localidade(),
                endereco.uf(),
                input.statusAluno(),
                input.escolaId());
    }

    private void validarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("numero é obrigatório quando cep é informado");
        }
    }
}
