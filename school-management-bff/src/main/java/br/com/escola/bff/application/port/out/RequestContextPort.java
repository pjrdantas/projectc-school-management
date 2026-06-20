package br.com.escola.bff.application.port.out;

import reactor.core.publisher.Mono;

import br.com.escola.bff.application.context.RequestContext;

public interface RequestContextPort {

    Mono<RequestContext> contextoAtual();
}

