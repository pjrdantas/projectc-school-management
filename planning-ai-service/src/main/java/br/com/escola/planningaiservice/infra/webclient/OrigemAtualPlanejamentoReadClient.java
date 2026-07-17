package br.com.escola.planningaiservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.AprovarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.application.dto.CriarVersaoConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.GerarConteudoIaRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoIaInteracaoResponse;
import br.com.escola.planningaiservice.application.exception.DownstreamUnavailableException;
import br.com.escola.planningaiservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.planningaiservice.application.port.out.PlanejamentoLeituraPort;

@Component
public class OrigemAtualPlanejamentoReadClient implements PlanejamentoLeituraPort {

    private final RestClient restClient;

    public OrigemAtualPlanejamentoReadClient(RestClient monolithPlanningAiRestClient) {
        this.restClient = monolithPlanningAiRestClient;
    }

    @Override
    public List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        try {
            List<BibliotecaConteudoPedagogicoResponse> response = restClient.get()
                    .uri(uriBuilder -> bibliotecaUri(uriBuilder, professorId, disciplinaId, tipoConteudo, tema))
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<BibliotecaConteudoPedagogicoResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Consulta da biblioteca pedagogica nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para consulta da biblioteca pedagogica",
                    exception);
        }
    }

    @Override
    public List<PlanejamentoIaInteracaoResponse> listarInteracoes(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId) {
        try {
            List<PlanejamentoIaInteracaoResponse> response = restClient.get()
                    .uri("/api/planejamentos-bimestrais/{planejamentoId}/ia/interacoes", planejamentoId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PlanejamentoIaInteracaoResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Consulta de interacoes de planejamento IA nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para consulta de interacoes de planejamento IA",
                    exception);
        }
    }

    @Override
    public List<ConteudoIaResponse> listarConteudos(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId) {
        try {
            List<ConteudoIaResponse> response = restClient.get()
                    .uri("/api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ConteudoIaResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Consulta de conteudos de planejamento IA nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para consulta de conteudos de planejamento IA",
                    exception);
        }
    }

    @Override
    public ConteudoIaResponse buscarConteudo(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        try {
            return restClient.get()
                    .uri("/api/ia/conteudos/{conteudoId}", conteudoId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(ConteudoIaResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Consulta de conteudo de planejamento IA nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para consulta de conteudo de planejamento IA",
                    exception);
        }
    }

    @Override
    public List<ConteudoIaVersaoResponse> listarVersoes(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        try {
            List<ConteudoIaVersaoResponse> response = restClient.get()
                    .uri("/api/ia/conteudos/{conteudoId}/versoes", conteudoId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ConteudoIaVersaoResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Consulta de versoes de conteudo de planejamento IA nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para consulta de versoes de conteudo de planejamento IA",
                    exception);
        }
    }

    @Override
    public ConteudoIaResponse gerarConteudo(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId,
            GerarConteudoIaRequest request) {
        try {
            return restClient.post()
                    .uri("/api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos", planejamentoId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .body(request)
                    .retrieve()
                    .body(ConteudoIaResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Geracao de conteudo de planejamento IA nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para geracao de conteudo de planejamento IA",
                    exception);
        }
    }

    @Override
    public ConteudoIaVersaoResponse criarVersao(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId,
            CriarVersaoConteudoIaRequest request) {
        try {
            return restClient.post()
                    .uri("/api/ia/conteudos/{conteudoId}/versoes", conteudoId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .body(request)
                    .retrieve()
                    .body(ConteudoIaVersaoResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Criacao de versao de conteudo de planejamento IA nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para criacao de versao de conteudo de planejamento IA",
                    exception);
        }
    }

    @Override
    public ConteudoIaResponse aprovarVersao(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId,
            AprovarVersaoConteudoIaRequest request) {
        try {
            return restClient.method(HttpMethod.PATCH)
                    .uri("/api/ia/conteudos/{conteudoId}/aprovar-versao", conteudoId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .body(request)
                    .retrieve()
                    .body(ConteudoIaResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Aprovacao de versao de conteudo de planejamento IA nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para aprovacao de versao de conteudo de planejamento IA",
                    exception);
        }
    }

    @Override
    public BibliotecaConteudoPedagogicoResponse publicarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId) {
        try {
            return restClient.post()
                    .uri("/api/ia/conteudos/{conteudoId}/publicar-biblioteca", conteudoId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(BibliotecaConteudoPedagogicoResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException(
                        "Publicacao de conteudo de planejamento IA na biblioteca nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para publicacao de conteudo de planejamento IA na biblioteca",
                    exception);
        }
    }

    private java.net.URI bibliotecaUri(
            UriBuilder uriBuilder,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        UriBuilder builder = uriBuilder.path("/api/biblioteca-conteudos-pedagogicos");
        if (professorId != null) {
            builder.queryParam("professorId", professorId);
        }
        if (disciplinaId != null) {
            builder.queryParam("disciplinaId", disciplinaId);
        }
        if (tipoConteudo != null && !tipoConteudo.isBlank()) {
            builder.queryParam("tipoConteudo", tipoConteudo);
        }
        if (tema != null && !tema.isBlank()) {
            builder.queryParam("tema", tema);
        }
        return builder.build();
    }
}

