package br.com.escola.bff.application.service;

public enum CatalogReadRoute {
    DISCIPLINAS("/api/disciplinas", "/internal/v1/disciplinas"),
    DISCIPLINA_POR_ID("/api/disciplinas/%s", "/internal/v1/disciplinas/%s"),
    PERIODOS_LETIVOS("/api/periodos-letivos", "/internal/v1/periodos-letivos"),
    PERIODO_LETIVO_POR_ID("/api/periodos-letivos/%s", "/internal/v1/periodos-letivos/%s"),
    SERIES("/api/series", "/internal/v1/series"),
    SERIE_POR_ID("/api/series/%s", "/internal/v1/series/%s"),
    TURNOS("/api/turnos", "/internal/v1/turnos"),
    TURNO_POR_ID("/api/turnos/%s", "/internal/v1/turnos/%s"),
    TURMAS("/api/turmas", "/internal/v1/turmas"),
    TURMA_POR_ID("/api/turmas/%s", "/internal/v1/turmas/%s"),
    TURMA_DISCIPLINAS("/api/turmas/%s/disciplinas", "/internal/v1/turmas/%s/disciplinas"),
    CATALOGOS_NIVEIS_ENSINO("/api/academico/catalogos/niveis-ensino", "/internal/v1/catalogos/niveis-ensino"),
    CATALOGOS_TURNOS("/api/academico/catalogos/turnos", "/internal/v1/turnos");

    private final String externalTemplate;
    private final String internalTemplate;

    CatalogReadRoute(String externalTemplate, String internalTemplate) {
        this.externalTemplate = externalTemplate;
        this.internalTemplate = internalTemplate;
    }

    public String externalPath(String... args) {
        return args.length == 0 ? externalTemplate : externalTemplate.formatted((Object[]) args);
    }

    public String internalPath(String... args) {
        return args.length == 0 ? internalTemplate : internalTemplate.formatted((Object[]) args);
    }
}
