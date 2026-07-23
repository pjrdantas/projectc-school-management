package br.com.escola.planningaiservice.application.context;

public final class InternalHeaders {

    public static final String INTERNAL_TOKEN = "X-Internal-Token";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String USUARIO_ID = "X-Usuario-Id";
    public static final String ESCOLA_ID = "X-Escola-Id";
    public static final String REQUEST_CONTEXT_ATTRIBUTE =
            "br.com.escola.planningaiservice.application.context.InternalRequestContext";

    private InternalHeaders() {
    }
}
