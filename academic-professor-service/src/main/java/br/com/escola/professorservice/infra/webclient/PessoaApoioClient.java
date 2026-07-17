package br.com.escola.professorservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.exception.DownstreamUnavailableException;
import br.com.escola.professorservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.professorservice.application.port.out.PessoaApoioPort;
import br.com.escola.professorservice.infra.config.PessoaApoioClientProperties;

@Component
public class PessoaApoioClient implements PessoaApoioPort {

    private final RestClient restClient;
    private final PessoaApoioClientProperties properties;

    public PessoaApoioClient(
            @Qualifier("pessoaApoioRestClient") RestClient pessoaApoioRestClient,
            PessoaApoioClientProperties properties) {
        this.restClient = pessoaApoioRestClient;
        this.properties = properties;
    }

    @Override
    public FuncionarioResumo buscarFuncionarioPorId(String authorization, InternalRequestContext context, UUID funcionarioId) {
        try {
            FuncionarioResumoResponse response = restClient.get()
                    .uri("/internal/v1/funcionarios/{funcionarioId}", funcionarioId)
                    .headers(headers -> enrichHeaders(headers, authorization, context, properties.internalToken()))
                    .retrieve()
                    .body(FuncionarioResumoResponse.class);
            if (response == null) {
                throw new DownstreamUnavailableException("People service retornou resposta vazia para funcionario");
            }
            return response.toPort();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException("Funcionario não encontrado");
            }
            throw new DownstreamUnavailableException("People service rejeitou consulta de funcionario", exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("People service indisponivel para consulta de funcionario", exception);
        }
    }

    @Override
    public List<FuncionarioResumo> listarFuncionariosAtivosPorEscola(String authorization, InternalRequestContext context) {
        try {
            List<FuncionarioResumoResponse> response = restClient.get()
                    .uri("/internal/v1/funcionarios")
                    .headers(headers -> enrichHeaders(headers, authorization, context, properties.internalToken()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FuncionarioResumoResponse>>() {
                    });
            return response == null ? List.of() : response.stream().map(FuncionarioResumoResponse::toPort).toList();
        } catch (RestClientResponseException exception) {
            throw new DownstreamUnavailableException("People service rejeitou listagem de funcionarios", exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("People service indisponivel para listagem de funcionarios", exception);
        }
    }

    @Override
    public PessoaResumo buscarPessoaPorId(String authorization, InternalRequestContext context, UUID pessoaId) {
        try {
            PessoaResumoResponse response = restClient.get()
                    .uri("/internal/v1/pessoas/{id}", pessoaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context, properties.internalToken()))
                    .retrieve()
                    .body(PessoaResumoResponse.class);
            if (response == null) {
                throw new DownstreamUnavailableException("People service retornou resposta vazia para pessoa");
            }
            return response.toPort();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException("Pessoa não encontrada");
            }
            throw new DownstreamUnavailableException("People service rejeitou consulta de pessoa", exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("People service indisponivel para consulta de pessoa", exception);
        }
    }

    private void enrichHeaders(
            HttpHeaders headers,
            String authorization,
            InternalRequestContext context,
            String internalToken) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.INTERNAL_TOKEN, internalToken);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }

    private record FuncionarioResumoResponse(
            UUID funcionarioId,
            UUID pessoaId,
            UUID escolaId,
            String nomeCompleto,
            String cargoDescricao,
            boolean ativo) {
        FuncionarioResumo toPort() {
            return new FuncionarioResumo(funcionarioId, pessoaId, escolaId, nomeCompleto, cargoDescricao, ativo);
        }
    }

    private record PessoaResumoResponse(
            UUID id,
            String nomeCompleto,
            UUID escolaId,
            String escolaNome,
            Boolean ativo) {
        PessoaResumo toPort() {
            return new PessoaResumo(id, nomeCompleto, escolaId, escolaNome, ativo);
        }
    }
}
