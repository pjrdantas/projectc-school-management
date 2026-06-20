package br.com.escola.bff.interfaces.web;

import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import br.com.escola.bff.application.context.RequestContext;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = correlationId(exchange.getRequest());
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(CORRELATION_ID_HEADER, correlationId))
                .build();
        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        RequestContext context = new RequestContext(correlationId, null, null);
        return chain.filter(exchange.mutate().request(request).build())
                .contextWrite(reactorContext -> reactorContext.put(RequestContext.REACTOR_CONTEXT_KEY, context));
    }

    private String correlationId(ServerHttpRequest request) {
        String received = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        return StringUtils.hasText(received) ? received.trim() : UUID.randomUUID().toString();
    }
}
