package br.com.escola.compartilhado.viacep;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ViaCepService {

    private final RestClient restClient;

    public ViaCepService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://viacep.com.br/ws").build();
    }

    public ViaCepResponse consultar(String cep) {
        String cepNormalizado = normalizar(cep);
        if (cepNormalizado == null) {
            return null;
        }
        if (!cepNormalizado.matches("\\d{8}")) {
            throw new CepInvalidoException();
        }

        try {
            ViaCepResponse response = restClient.get()
                    .uri("/{cep}/json", cepNormalizado)
                    .retrieve()
                    .body(ViaCepResponse.class);

            if (response == null || response.inexistente()) {
                throw new CepNaoEncontradoException(cepNormalizado);
            }

            return response;
        } catch (CepNaoEncontradoException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new CepNaoEncontradoException(cepNormalizado);
            }
            throw new ViaCepIndisponivelException();
        } catch (RestClientException exception) {
            throw new ViaCepIndisponivelException();
        }
    }

    public String normalizar(String cep) {
        if (cep == null || cep.isBlank()) {
            return null;
        }
        return cep.replaceAll("\\D", "");
    }
}
