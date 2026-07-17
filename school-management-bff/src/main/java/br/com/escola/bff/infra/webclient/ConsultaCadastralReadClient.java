package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.ConsultaCadastralReadPort;
import br.com.escola.bff.infra.config.CadastroPessoaClientProperties;
import reactor.core.publisher.Mono;

@Component
public class ConsultaCadastralReadClient extends AbstractDownstreamClientSupport implements ConsultaCadastralReadPort {

    private final WebClient webClient;
    private final CadastroPessoaClientProperties properties;

    public ConsultaCadastralReadClient(
            @Qualifier("peopleServiceWebClient")
            WebClient peopleServiceWebClient,
            CadastroPessoaClientProperties properties) {
        this.webClient = peopleServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultarCadastro(
            CatalogReadQuery query,
            AuthSessionContext context,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        var uriBuilder = UriComponentsBuilder.fromPath("/internal/v1/pessoas/consulta-cadastral")
                .queryParam("page", page)
                .queryParam("size", size);
        if (StringUtils.hasText(nomeAluno)) {
            uriBuilder.queryParam("nomeAluno", nomeAluno);
        }
        if (StringUtils.hasText(cpfAluno)) {
            uriBuilder.queryParam("cpfAluno", cpfAluno);
        }
        if (StringUtils.hasText(nomeResponsavel)) {
            uriBuilder.queryParam("nomeResponsavel", nomeResponsavel);
        }
        if (StringUtils.hasText(cpfResponsavel)) {
            uriBuilder.queryParam("cpfResponsavel", cpfResponsavel);
        }
        return webClient.get()
                .uri(uriBuilder.build(true).toUriString())
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }
}


