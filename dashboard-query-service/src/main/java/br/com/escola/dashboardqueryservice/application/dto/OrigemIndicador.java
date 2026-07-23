package br.com.escola.dashboardqueryservice.application.dto;

import java.util.Locale;

public enum OrigemIndicador {

    MATRICULA_DOCUMENTO,
    PESSOAS,
    CATALOGO,
    PROFESSORES,
    PEDAGOGICO,
    PLANEJAMENTO;

    public boolean aceita(String codigoIndicador) {
        String codigo = codigoIndicador.trim().toUpperCase(Locale.ROOT);
        return switch (this) {
            case MATRICULA_DOCUMENTO -> codigo.equals("TOTAL_MATRICULAS")
                    || codigo.startsWith("MATRICULAS_") || codigo.equals("TRANSFERENCIAS");
            case PESSOAS -> codigo.equals("ALUNOS_ATIVOS") || codigo.equals("ALUNOS_INATIVOS")
                    || codigo.equals("SOLICITACOES_EXCLUSAO_PENDENTES");
            case CATALOGO -> codigo.equals("TURMAS_ATIVAS") || codigo.equals("TURMAS_LOTADAS")
                    || codigo.startsWith("TURMAS_");
            case PROFESSORES -> codigo.equals("PROFESSORES_ALOCADOS") || codigo.startsWith("PROFESSOR_");
            case PEDAGOGICO -> codigo.startsWith("AULAS_") || codigo.startsWith("FREQUENCIAS_")
                    || codigo.startsWith("AVALIACOES_") || codigo.startsWith("BOLETINS_")
                    || codigo.startsWith("HISTORICOS_") || codigo.equals("ALUNOS_APROVADOS")
                    || codigo.equals("ALUNOS_REPROVADOS");
            case PLANEJAMENTO -> codigo.startsWith("PLANEJAMENTOS_");
        };
    }
}
