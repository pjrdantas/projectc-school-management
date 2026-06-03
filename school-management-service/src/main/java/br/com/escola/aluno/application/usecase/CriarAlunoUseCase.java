package br.com.escola.aluno.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.aluno.application.dto.AlunoInput;
import br.com.escola.aluno.application.dto.AlunoOutput;
import br.com.escola.aluno.application.port.out.AlunoCommandGateway;
import br.com.escola.compartilhado.viacep.ViaCepResponse;
import br.com.escola.compartilhado.viacep.ViaCepService;
import br.com.escola.aluno.domain.exception.AlunoJaCadastradoException;

@Service
public class CriarAlunoUseCase {

    private final AlunoCommandGateway alunoCommandGateway;
    private final ViaCepService viaCepService;

    public CriarAlunoUseCase(AlunoCommandGateway alunoCommandGateway, ViaCepService viaCepService) {
        this.alunoCommandGateway = alunoCommandGateway;
        this.viaCepService = viaCepService;
    }

    public AlunoOutput executar(AlunoInput input) {
        if (alunoCommandGateway.existsByCpf(input.cpf())) {
            throw new AlunoJaCadastradoException();
        }
        return alunoCommandGateway.save(preencherEndereco(input));
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
                input.statusAluno());
    }

    private void validarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("numero é obrigatório quando cep é informado");
        }
    }
}
