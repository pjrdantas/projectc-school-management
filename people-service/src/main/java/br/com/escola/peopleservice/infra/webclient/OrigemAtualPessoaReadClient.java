package br.com.escola.peopleservice.infra.webclient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculadoResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.exception.DownstreamUnavailableException;
import br.com.escola.peopleservice.application.port.out.PessoaReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OrigemAtualPessoaReadClient implements PessoaReadPort {

    private static final PessoaConsultaCadastralPageResponse EMPTY_PAGE =
            new PessoaConsultaCadastralPageResponse(List.of(), 0L, 0, 0);

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public OrigemAtualPessoaReadClient(RestClient monolithPeopleRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithPeopleRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposPessoa(String authorization, InternalRequestContext context) {
        try {
            List<PessoaCatalogoResponse> response = restClient.get()
                    .uri("/internal/pessoas/catalogos/tipos-pessoa")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PessoaCatalogoResponse>>() {
                    });
            registrarRequisicao("listarTiposPessoa", "success");
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            registrarErro("listarTiposPessoa", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listarTiposPessoa", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para catalogos de pessoa", exception);
        }
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context) {
        try {
            List<PessoaCatalogoResponse> response = restClient.get()
                    .uri("/internal/pessoas/catalogos/tipos-endereco")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PessoaCatalogoResponse>>() {
                    });
            registrarRequisicao("listarTiposEndereco", "success");
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            registrarErro("listarTiposEndereco", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listarTiposEndereco", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para catalogos de endereco", exception);
        }
    }

    @Override
    public Optional<PessoaResumoResponse> buscarPessoaPorId(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId) {
        try {
            PessoaResumoResponse response = restClient.get()
                    .uri("/internal/pessoas/{id}", pessoaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(PessoaResumoResponse.class);
            registrarRequisicao("buscarPorId", "success");
            return Optional.ofNullable(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                registrarRequisicao("buscarPorId", "not_found");
                return Optional.empty();
            }
            registrarErro("buscarPorId", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("buscarPorId", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura interna de pessoa", exception);
        }
    }

    @Override
    public PessoaConsultaCadastralPageResponse consultarCadastro(
            String authorization,
            InternalRequestContext context,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        try {
            PessoaConsultaCadastralPageResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/pessoas/consulta-cadastral")
                            .queryParamIfPresent("nomeAluno", Optional.ofNullable(nomeAluno))
                            .queryParamIfPresent("cpfAluno", Optional.ofNullable(cpfAluno))
                            .queryParamIfPresent("nomeResponsavel", Optional.ofNullable(nomeResponsavel))
                            .queryParamIfPresent("cpfResponsavel", Optional.ofNullable(cpfResponsavel))
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(PessoaConsultaCadastralPageResponse.class);
            registrarRequisicao("consultarCadastro", "success");
            return response == null ? EMPTY_PAGE : response;
        } catch (RestClientResponseException exception) {
            registrarErro("consultarCadastro", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("consultarCadastro", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta cadastral de pessoas", exception);
        }
    }

    @Override
    public Optional<List<PessoaResponsavelVinculadoResponse>> listarResponsaveisPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        try {
            List<PessoaResponsavelVinculadoResponse> response = restClient.get()
                    .uri("/api/alunos/{alunoId}/responsaveis", alunoId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PessoaResponsavelVinculadoResponse>>() {
                    });
            registrarRequisicao("listarResponsaveisPorAluno", "success");
            return Optional.of(response == null ? List.of() : response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                registrarRequisicao("listarResponsaveisPorAluno", "not_found");
                return Optional.empty();
            }
            registrarErro("listarResponsaveisPorAluno", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listarResponsaveisPorAluno", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura de responsaveis do aluno", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }

    private void registrarRequisicao(String operacao, String resultado) {
        meterRegistry.counter(
                "people.monolith.requests",
                "operacao", operacao,
                "destino", "monolith",
                "resultado", resultado)
                .increment();
    }

    private void registrarErro(String operacao, Exception exception) {
        registrarRequisicao(operacao, "error");
        meterRegistry.counter(
                "people.monolith.failures",
                "operacao", operacao,
                "causa", exception.getClass().getSimpleName())
                .increment();
    }
}


