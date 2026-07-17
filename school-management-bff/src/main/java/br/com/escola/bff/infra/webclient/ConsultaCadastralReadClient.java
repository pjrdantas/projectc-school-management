package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/internal/v1/pessoas/consulta-cadastral")
                            .queryParam("page", page)
                            .queryParam("size", size);
                    if (nomeAluno != null) {
                        builder.queryParam("nomeAluno", nomeAluno);
                    }
                    if (cpfAluno != null) {
                        builder.queryParam("cpfAluno", cpfAluno);
                    }
                    if (nomeResponsavel != null) {
                        builder.queryParam("nomeResponsavel", nomeResponsavel);
                    }
                    if (cpfResponsavel != null) {
                        builder.queryParam("cpfResponsavel", cpfResponsavel);
                    }
                    return builder.build();
                })
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


