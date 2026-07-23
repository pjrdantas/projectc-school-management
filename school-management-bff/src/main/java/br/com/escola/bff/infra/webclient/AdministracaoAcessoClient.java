package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AdministracaoAcessoCommand;
import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.model.OperacaoAcesso;
import br.com.escola.bff.application.port.out.AdministracaoAcessoPort;
import br.com.escola.bff.infra.config.AutenticacaoClientProperties;
import reactor.core.publisher.Mono;

@Component
public class AdministracaoAcessoClient extends AbstractDownstreamClientSupport implements AdministracaoAcessoPort {

    private final WebClient webClient;
    private final AutenticacaoClientProperties properties;

    public AdministracaoAcessoClient(
            @Qualifier("identityAccessServiceWebClient") WebClient identityAccessServiceWebClient,
            AutenticacaoClientProperties properties) {
        this.webClient = identityAccessServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(
            AdministracaoAcessoCommand command,
            AuthSessionContext context) {
        WebClient.RequestBodySpec request = webClient.method(method(command.operacao()))
                .uri(path(command))
                .header(HttpHeaders.AUTHORIZATION, command.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", command.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString());

        WebClient.RequestHeadersSpec<?> exchange = command.requestBody() == null
                ? request
                : request.contentType(MediaType.APPLICATION_JSON).bodyValue(command.requestBody());
        return exchange
                .exchangeToMono(response -> handle(response, "Servico de acesso retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Servico de acesso indisponivel"));
    }

    private String path(AdministracaoAcessoCommand command) {
        String path = "/internal/v1/" + command.recurso().path();
        return command.recursoId() == null ? path : path + "/" + command.recursoId();
    }

    private HttpMethod method(OperacaoAcesso operacao) {
        return switch (operacao) {
            case LISTAR, BUSCAR -> HttpMethod.GET;
            case CRIAR -> HttpMethod.POST;
            case ATUALIZAR -> HttpMethod.PUT;
            case EXCLUIR -> HttpMethod.DELETE;
        };
    }
}
