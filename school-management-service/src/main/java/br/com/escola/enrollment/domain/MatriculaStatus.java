package br.com.escola.enrollment.domain;

public enum MatriculaStatus {
    SOLICITADA,
    EM_ANDAMENTO,
    AGUARDANDO_DOCUMENTOS,
    AGUARDANDO_HISTORICO_ESCOLAR,
    EFETIVADA,
    CANCELADA,
    INDEFERIDA,
    TRANSFERIDO;

    public boolean ocupaVaga() {
        return this != CANCELADA && this != INDEFERIDA && this != TRANSFERIDO;
    }
}
