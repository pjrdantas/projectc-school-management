package br.com.escola.responsavel.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.responsavel.application.dto.ResponsavelInput;
import br.com.escola.responsavel.application.dto.ResponsavelOutput;
import br.com.escola.responsavel.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavel.domain.exception.ResponsavelJaCadastradoException;
import br.com.escola.compartilhado.viacep.ViaCepResponse;
import br.com.escola.compartilhado.viacep.ViaCepService;

@Service
public class CriarResponsavelUseCase {

    private final ResponsavelCommandGateway responsavelCommandGateway;
    private final ViaCepService viaCepService;

    public CriarResponsavelUseCase(
            ResponsavelCommandGateway responsavelCommandGateway,
            ViaCepService viaCepService) {
        this.responsavelCommandGateway = responsavelCommandGateway;
        this.viaCepService = viaCepService;
    }

    public ResponsavelOutput executar(ResponsavelInput input) {
        if (responsavelCommandGateway.existsByCpf(input.cpf(), input.escolaId())) {
            throw new ResponsavelJaCadastradoException();
        }
        return responsavelCommandGateway.save(preencherEndereco(input));
    }

    private ResponsavelInput preencherEndereco(ResponsavelInput input) {
        ViaCepResponse endereco = viaCepService.consultar(input.cep());
        if (endereco == null) {
            return input;
        }
        validarNumero(input.numero());
        return new ResponsavelInput(
                input.nomeCompleto(),
                input.cpf(),
                input.email(),
                input.telefone(),
                input.rg(),
                viaCepService.normalizar(input.cep()),
                endereco.logradouro(),
                input.numero(),
                input.complemento(),
                endereco.bairro(),
                endereco.localidade(),
                endereco.uf(),
                input.escolaId());
    }

    private void validarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("numero é obrigatório quando cep é informado");
        }
    }
}
