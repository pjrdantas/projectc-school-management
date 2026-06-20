package br.com.escola.bff.infra.context;

import org.springframework.stereotype.Component;

import br.com.escola.bff.application.context.RequestContext;
import br.com.escola.bff.application.port.out.RequestContextPort;
import reactor.core.publisher.Mono;

@Component
public class ReactorRequestContextAdapter implements RequestContextPort {

    @Override
    public Mono<RequestContext> contextoAtual() {
        return Mono.deferContextual(context -> Mono.justOrEmpty(
                context.getOrEmpty(RequestContext.REACTOR_CONTEXT_KEY)));
    }
}
