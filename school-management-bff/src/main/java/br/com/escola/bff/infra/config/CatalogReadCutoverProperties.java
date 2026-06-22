package br.com.escola.bff.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import br.com.escola.bff.application.service.CatalogReadRoute;

@ConfigurationProperties("features.catalog-read-cutover")
public record CatalogReadCutoverProperties(
        boolean enabled,
        String reportPath,
        boolean fallbackToMonolithOnError,
        RouteFlags routes
) {

    public boolean routeEnabled(CatalogReadRoute route) {
        return switch (route) {
            case DISCIPLINAS -> routes.disciplinas();
            case DISCIPLINA_POR_ID -> routes.disciplinaById();
            case PERIODOS_LETIVOS -> routes.periodosLetivos();
            case PERIODO_LETIVO_POR_ID -> routes.periodoLetivoById();
            case SERIES -> routes.series();
            case SERIE_POR_ID -> routes.serieById();
            case TURNOS -> routes.turnos();
            case TURNO_POR_ID -> routes.turnoById();
            case TURMAS -> routes.turmas();
            case TURMA_POR_ID -> routes.turmaById();
            case TURMA_DISCIPLINAS -> routes.turmaDisciplinas();
            case CATALOGOS_NIVEIS_ENSINO -> routes.catalogosNiveisEnsino();
            case CATALOGOS_TURNOS -> routes.catalogosTurnos();
        };
    }

    public record RouteFlags(
            boolean disciplinas,
            boolean disciplinaById,
            boolean periodosLetivos,
            boolean periodoLetivoById,
            boolean series,
            boolean serieById,
            boolean turnos,
            boolean turnoById,
            boolean turmas,
            boolean turmaById,
            boolean turmaDisciplinas,
            boolean catalogosNiveisEnsino,
            boolean catalogosTurnos
    ) {}
}
