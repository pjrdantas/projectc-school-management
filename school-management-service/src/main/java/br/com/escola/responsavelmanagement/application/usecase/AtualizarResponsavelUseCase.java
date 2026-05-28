package br.com.escola.responsavelmanagement.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.responsavelmanagement.application.dto.ResponsavelInput;
import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelJaCadastradoException;
import br.com.escola.shared.viacep.ViaCepResponse;
import br.com.escola.shared.viacep.ViaCepService;

@Service
public class AtualizarResponsavelUseCase {

    private final ResponsavelCommandGateway responsavelCommandGateway;
    private final ViaCepService viaCepService;

    public AtualizarResponsavelUseCase(
            ResponsavelCommandGateway responsavelCommandGateway,
            ViaCepService viaCepService) {
        this.responsavelCommandGateway = responsavelCommandGateway;
        this.viaCepService = viaCepService;
    }

    public ResponsavelOutput executar(UUID id, ResponsavelInput input) {
        if (responsavelCommandGateway.existsByCpfAndIdNot(input.cpf(), id)) {
            throw new ResponsavelJaCadastradoException();
        }
        return responsavelCommandGateway.update(id, preencherEndereco(input));
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
                endereco.uf());
    }

    private void validarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("numero é obrigatório quando cep é informado");
        }
    }
}
