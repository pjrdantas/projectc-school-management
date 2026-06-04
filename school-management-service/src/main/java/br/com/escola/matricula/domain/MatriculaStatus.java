package br.com.escola.matricula.domain;

public enum MatriculaStatus {
    SOLICITADA,
    EM_ANDAMENTO,
    AGUARDANDO_DOCUMENTOS,
    AGUARDANDO_HISTORICO_ESCOLAR,
    EFETIVADA,
    CONCLUIDA,
    CANCELADA,
    INDEFERIDA,
    TRANSFERIDO;

    public boolean ocupaVaga() {
        return this != CANCELADA && this != INDEFERIDA && this != TRANSFERIDO;
    }
}
