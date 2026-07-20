package br.com.escola.identityaccessservice.infra.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import br.com.escola.identityaccessservice.application.context.InternalHeaders;
import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.exception.InternalApiUnauthorizedException;
import br.com.escola.identityaccessservice.application.exception.InvalidRequestContextException;
import br.com.escola.identityaccessservice.application.port.out.AutorizacaoAdministrativaPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class InternalApiInterceptor implements HandlerInterceptor {

    private final byte[] configuredToken;
    private final AutorizacaoAdministrativaPort autorizacaoAdministrativaPort;

    public InternalApiInterceptor(
            @Value("${identity-access.internal-api.token:}") String configuredToken,
            AutorizacaoAdministrativaPort autorizacaoAdministrativaPort) {
        this.configuredToken = configuredToken.getBytes(StandardCharsets.UTF_8);
        this.autorizacaoAdministrativaPort = autorizacaoAdministrativaPort;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        authorize(request.getHeader(InternalHeaders.INTERNAL_TOKEN));

        String correlationId = requiredHeader(request, InternalHeaders.CORRELATION_ID);
        boolean authenticationLifecycle = request.getRequestURI().matches(
                ".*/auth/(login|refresh|logout)$");
        UUID usuarioId = authenticationLifecycle ? null : uuidHeader(request, InternalHeaders.USUARIO_ID);
        UUID escolaId = authenticationLifecycle ? null : uuidHeader(request, InternalHeaders.ESCOLA_ID);
        InternalRequestContext context = new InternalRequestContext(correlationId, usuarioId, escolaId);

        if (request.getRequestURI().matches(".*/(usuarios|perfis|permissoes)(/[^/]+)?$")) {
            autorizacaoAdministrativaPort.autorizar(usuarioId, autoridade(request));
        }

        request.setAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE, context);
        response.setHeader(InternalHeaders.CORRELATION_ID, correlationId);
        return true;
    }

    private String autoridade(HttpServletRequest request) {
        return switch (request.getMethod()) {
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            case "GET" -> request.getRequestURI().matches(".*/[^/]+/[^/]+$") ? "READ" : "READ_ALL";
            default -> throw new InvalidRequestContextException("Metodo administrativo nao suportado");
        };
    }

    private void authorize(String providedToken) {
        if (configuredToken.length == 0 || providedToken == null
                || !MessageDigest.isEqual(configuredToken, providedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new InternalApiUnauthorizedException("Credencial interna ausente ou invalida");
        }
    }

    private UUID uuidHeader(HttpServletRequest request, String name) {
        String value = requiredHeader(request, name);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestContextException("Header " + name + " deve conter um UUID valido");
        }
    }

    private String requiredHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value == null || value.isBlank()) {
            throw new InvalidRequestContextException("Header obrigatorio ausente: " + name);
        }
        return value.trim();
    }
}
