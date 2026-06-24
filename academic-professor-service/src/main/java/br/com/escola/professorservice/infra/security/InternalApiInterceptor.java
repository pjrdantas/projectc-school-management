package br.com.escola.professorservice.infra.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.exception.InternalApiUnauthorizedException;
import br.com.escola.professorservice.application.exception.InvalidRequestContextException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class InternalApiInterceptor implements HandlerInterceptor {

    private final byte[] configuredToken;

    public InternalApiInterceptor(@Value("${professor.shadow.internal-api.token:}") String configuredToken) {
        this.configuredToken = configuredToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        authorize(request.getHeader(InternalHeaders.INTERNAL_TOKEN));

        String correlationId = requiredHeader(request, InternalHeaders.CORRELATION_ID);
        UUID usuarioId = uuidHeader(request, InternalHeaders.USUARIO_ID);
        UUID escolaId = uuidHeader(request, InternalHeaders.ESCOLA_ID);
        InternalRequestContext context = new InternalRequestContext(correlationId, usuarioId, escolaId);

        request.setAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE, context);
        response.setHeader(InternalHeaders.CORRELATION_ID, correlationId);
        return true;
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
