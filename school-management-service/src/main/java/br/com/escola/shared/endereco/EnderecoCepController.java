package br.com.escola.shared.endereco;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.shared.viacep.ViaCepResponse;
import br.com.escola.shared.viacep.ViaCepService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/enderecos")
public class EnderecoCepController {

    private final ViaCepService viaCepService;

    public EnderecoCepController(ViaCepService viaCepService) {
        this.viaCepService = viaCepService;
    }

    @GetMapping("/cep/{cep}")
    @Operation(summary = "Consulta endereço por CEP")
    public EnderecoCepResponse consultarCep(@PathVariable String cep) {
        ViaCepResponse response = viaCepService.consultar(cep);
        return new EnderecoCepResponse(
                viaCepService.normalizar(cep),
                response.logradouro(),
                response.bairro(),
                response.localidade(),
                response.uf(),
                response.complemento());
    }
}
