package br.com.escola.bff.application.service;

public enum CatalogWriteRoute {
    PERIODOS_LETIVOS("/api/periodos-letivos", "/internal/v1/periodos-letivos"),
    DISCIPLINAS("/api/disciplinas", "/internal/v1/disciplinas"),
    SERIES("/api/series", "/internal/v1/series"),
    TURMAS("/api/turmas", "/internal/v1/turmas");

    private final String externalPath;
    private final String internalPath;

    CatalogWriteRoute(String externalPath, String internalPath) {
        this.externalPath = externalPath;
        this.internalPath = internalPath;
    }

    public String externalPath() {
        return externalPath;
    }

    public String internalPath() {
        return internalPath;
    }
}
