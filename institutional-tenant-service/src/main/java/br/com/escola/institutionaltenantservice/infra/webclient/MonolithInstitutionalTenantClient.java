package br.com.escola.institutionaltenantservice.infra.webclient;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.institutionaltenantservice.application.context.InternalRequestContext;
import br.com.escola.institutionaltenantservice.application.dto.TenantEscolaResponse;
import br.com.escola.institutionaltenantservice.application.exception.DownstreamUnavailableException;
import br.com.escola.institutionaltenantservice.application.exception.InstitutionalTenantServiceResourceNotFoundException;
import br.com.escola.institutionaltenantservice.application.port.out.InstitutionalTenantPort;

@Component
public class MonolithInstitutionalTenantClient implements InstitutionalTenantPort {

    private final RestClient restClient;

    public MonolithInstitutionalTenantClient(RestClient institutionalTenantMonolithRestClient) {
        this.restClient = institutionalTenantMonolithRestClient;
    }

    @Override
    public List<TenantEscolaResponse> listarEscolasDisponiveis(
            String authorization,
            InternalRequestContext context) {
        try {
            List<TenantEscolaResponse> response = restClient.get()
                    .uri("/internal/auth/escolas")
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TenantEscolaResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new InstitutionalTenantServiceResourceNotFoundException(
                        "Consulta de escolas disponiveis nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta de tenant institucional", exception);
        }
    }
}
