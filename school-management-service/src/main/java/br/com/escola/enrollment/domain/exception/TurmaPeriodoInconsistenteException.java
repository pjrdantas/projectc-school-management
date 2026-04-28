package br.com.escola.enrollment.domain.exception;

import java.util.UUID;

public class TurmaPeriodoInconsistenteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TurmaPeriodoInconsistenteException(UUID turmaId, UUID periodoLetivoId) {
        super("A turma " + turmaId + " não pertence ao período letivo " + periodoLetivoId);
    }
}
